package co.edu.sgpg.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import co.edu.sgpg.config.EntregaArchivoConfiguration;
import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.*;
import co.edu.sgpg.service.dto.EntregaDTO;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.impl.EntregaServiceImpl;
import co.edu.sgpg.service.mapper.EntregaMapperImpl;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.unit.DataSize;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class EntregaArchivoTest {

    private static final byte[] PDF = "%PDF-1.7\nprueba\n".getBytes(StandardCharsets.US_ASCII);

    @Mock
    private EntregaRepository entregas;

    @Mock
    private FechaLimiteEtapaRepository fechas;

    @Mock
    private ObservacionRepository observaciones;

    @Mock
    private ProyectoAccessService acceso;

    @Mock
    private PermisosEntidadService permisos;

    @Mock
    private VisibilidadProyectoService visibilidad;

    @Mock
    private AvanceService avance;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EntregaServiceImpl servicio;
    private final EntregaMapperImpl mapper = new EntregaMapperImpl();
    private ProyectoGrado proyecto;

    @BeforeEach
    void preparar() {
        servicio = new EntregaServiceImpl(
            entregas,
            mapper,
            fechas,
            observaciones,
            acceso,
            permisos,
            visibilidad,
            avance,
            Clock.systemUTC(),
            eventPublisher,
            DataSize.ofBytes(PDF.length)
        );
        proyecto = new ProyectoGrado().id(1L).estado(EstadoProyecto.ACTIVO).etapaActual(Etapa.PROPUESTA);
    }

    private EntregaDTO dto() {
        EntregaDTO dto = new EntregaDTO();
        dto.setId(2L);
        dto.setTitulo("Documento");
        ProyectoGradoDTO relacion = new ProyectoGradoDTO();
        relacion.setId(1L);
        dto.setProyecto(relacion);
        dto.setArchivo(PDF.clone());
        dto.setArchivoContentType("application/pdf");
        dto.setArchivoNombre("Propuesta académica.pdf");
        return dto;
    }

    private EntregaDTO ejecutar(String operacion, EntregaDTO dto) {
        if (operacion.equals("crear")) {
            when(permisos.proyecto(1L)).thenReturn(proyecto);
            return servicio.save(dto);
        }
        when(entregas.findById(2L)).thenReturn(Optional.of(new Entrega().id(2L).proyecto(proyecto).estado(EstadoEntrega.ENVIADA)));
        return operacion.equals("editar") ? servicio.update(dto) : servicio.partialUpdate(dto).orElseThrow();
    }

    private void regla(String codigo, Runnable accion) {
        assertThatThrownBy(accion::run).isInstanceOfSatisfying(ReglaNegocioException.class, error ->
            assertThat(error.getCodigo()).isEqualTo(codigo)
        );
        verify(entregas, never()).save(any());
        verifyNoInteractions(avance);
    }

    @ParameterizedTest
    @ValueSource(strings = { "crear", "editar", "parcial" })
    void aceptaPdfHastaElLimiteYMapeaSoloMetadatos(String operacion) {
        when(entregas.save(any())).thenAnswer(inv -> inv.getArgument(0));
        EntregaDTO resultado = ejecutar(operacion, dto());
        assertThat(resultado.isTieneArchivo()).isTrue();
        assertThat(resultado.getArchivo()).isNull();
        assertThat(resultado.getArchivoContentType()).isEqualTo("application/pdf");
        assertThat(resultado.getArchivoNombre()).isEqualTo("Propuesta académica.pdf");
        verify(entregas).save(argThat(entrega -> Arrays.equals(entrega.getArchivo(), PDF)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "crear", "editar", "parcial" })
    void rechazaUnByteSobreElMaximoConfigurado(String operacion) {
        EntregaDTO dto = dto();
        dto.setArchivo(Arrays.copyOf(PDF, PDF.length + 1));
        regla("ARCHIVO_DEMASIADO_GRANDE", () -> ejecutar(operacion, dto));
    }

    @ParameterizedTest
    @CsvSource({
        "crear,application/pdf,falso",
        "editar,application/pdf,falso",
        "parcial,application/pdf,falso",
        "crear,text/plain,%PDF-",
        "editar,text/plain,%PDF-",
        "parcial,text/plain,%PDF-",
        "crear,application/pdf,%PDF",
        "crear,application/pdf,''",
        "crear,application/pdf,xx%PDF-",
    })
    void exigeTipoYFirmaAlInicio(String operacion, String tipo, String contenido) {
        EntregaDTO dto = dto();
        dto.setArchivoContentType(tipo);
        dto.setArchivo(contenido.getBytes(StandardCharsets.US_ASCII));
        regla("ARCHIVO_NO_PDF", () -> ejecutar(operacion, dto));
    }

    @Test
    void rechazaPdfSinContentType() {
        EntregaDTO dto = dto();
        dto.setArchivoContentType(null);
        regla("ARCHIVO_NO_PDF", () -> ejecutar("crear", dto));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void exigeDocumentoEnTodaEntregaNueva(String url) {
        EntregaDTO dto = dto();
        dto.setArchivo(null);
        dto.setUrlDocumento(url);
        regla("DOCUMENTO_REQUERIDO", () -> ejecutar("crear", dto));
    }

    @Test
    void admiteSoloUrlYDescartaMetadatosSinArchivo() {
        EntregaDTO dto = dto();
        dto.setArchivo(null);
        dto.setUrlDocumento("https://example.org/propuesta.pdf");
        when(entregas.save(any())).thenAnswer(inv -> inv.getArgument(0));
        EntregaDTO resultado = ejecutar("crear", dto);
        assertThat(resultado.isTieneArchivo()).isFalse();
        assertThat(resultado.getArchivoNombre()).isNull();
        assertThat(resultado.getArchivoContentType()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void editarSinBytesConservaArchivoYNombre(boolean parcial) {
        Entrega entrega = new Entrega()
            .id(2L)
            .proyecto(proyecto)
            .estado(EstadoEntrega.ENVIADA)
            .archivo(PDF)
            .archivoContentType("application/pdf")
            .archivoNombre("original.pdf");
        when(entregas.findById(2L)).thenReturn(Optional.of(entrega));
        when(entregas.save(any())).thenAnswer(inv -> inv.getArgument(0));
        EntregaDTO dto = dto();
        dto.setArchivo(null);
        dto.setArchivoNombre("no reemplazar.pdf");
        dto.setArchivoContentType("text/plain");
        if (parcial) servicio.partialUpdate(dto);
        else servicio.update(dto);
        assertThat(entrega.getArchivo()).isEqualTo(PDF);
        assertThat(entrega.getArchivoNombre()).isEqualTo("original.pdf");
        assertThat(entrega.getArchivoContentType()).isEqualTo("application/pdf");
    }

    @Test
    void descargaCompruebaVisibilidadYUsaNombreDeRespaldo() {
        when(entregas.findById(2L)).thenReturn(Optional.of(new Entrega().proyecto(proyecto).archivo(PDF)));
        var archivo = servicio.obtenerArchivo(2L).orElseThrow();
        verify(acceso).verificarPuedeVer(1L);
        assertThat(archivo.contenido()).isEqualTo(PDF);
        assertThat(archivo.nombre()).isEqualTo("entrega.pdf");
    }

    @Test
    void descargaNoExisteSinEntregaOSinBytes() {
        assertThat(servicio.obtenerArchivo(9L)).isEmpty();
        when(entregas.findById(2L)).thenReturn(Optional.of(new Entrega().proyecto(proyecto)));
        assertThat(servicio.obtenerArchivo(2L)).isEmpty();
        verify(acceso).verificarPuedeVer(1L);
    }

    @Test
    void noDescargaSinPermisoInclusoSiNoHayArchivo() {
        when(entregas.findById(2L)).thenReturn(Optional.of(new Entrega().proyecto(proyecto)));
        doThrow(new AccessDeniedException("Sin permiso")).when(acceso).verificarPuedeVer(1L);
        assertThatThrownBy(() -> servicio.obtenerArchivo(2L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void bytesSoloSeDeserializanYElIndicadorNoLoDecideElCliente() {
        var om = JsonMapper.builder().build();
        EntregaDTO dto = dto();
        assertThat(om.readTree(om.writeValueAsString(dto)).has("archivo")).isFalse();
        EntregaDTO recibido = om.readValue(om.writeValueAsString(Map.of("archivo", PDF, "tieneArchivo", true)), EntregaDTO.class);
        assertThat(recibido.getArchivo()).isEqualTo(PDF);
        assertThat(recibido.isTieneArchivo()).isFalse();
    }

    @Test
    void jsonPermiteDiezMegabytesYUnByteParaQueElServicioDevuelva409() {
        var factory = JsonFactory.builder();
        new EntregaArchivoConfiguration().limiteJsonEntregas(DataSize.ofMegabytes(10)).customize(factory);
        var om = JsonMapper.builder(factory.build()).build();
        byte[] grande = Arrays.copyOf(PDF, 10 * 1024 * 1024 + 1);
        EntregaDTO recibido = om.readValue(om.writeValueAsBytes(Map.of("archivo", grande)), EntregaDTO.class);
        assertThat(recibido.getArchivo()).hasSize(grande.length);
    }

    @Test
    void limiteJsonSeAdaptaATamanoConfiguradoSinCambiarOtrasRestricciones() {
        var factory = JsonFactory.builder().streamReadConstraints(StreamReadConstraints.defaults().rebuild().maxNestingDepth(80).build());
        new EntregaArchivoConfiguration().limiteJsonEntregas(DataSize.ofMegabytes(30)).customize(factory);
        var restricciones = factory.build().streamReadConstraints();
        assertThat(restricciones.getMaxStringLength()).isGreaterThan(40 * 1024 * 1024);
        assertThat(restricciones.getMaxNestingDepth()).isEqualTo(80);
    }

    @Test
    void liquibaseBlobCreaOidYRequiereMigracionABytea() {
        var postgres = new PostgresDatabase();
        assertThat(
            DataTypeFactory.getInstance().fromDescription("blob", postgres).toDatabaseDataType(postgres).toString()
        ).isEqualToIgnoringCase("oid");
        assertThat(
            DataTypeFactory.getInstance().fromDescription("bytea", postgres).toDatabaseDataType(postgres).toString()
        ).isEqualToIgnoringCase("bytea");
    }
}
