package co.edu.sgpg.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.*;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.*;
import co.edu.sgpg.service.dto.*;
import co.edu.sgpg.service.impl.*;
import co.edu.sgpg.service.mapper.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

/** Reglas de dominio sin contexto Spring ni base de datos; el tiempo siempre es determinista. */
@ExtendWith(MockitoExtension.class)
class ReglasEntidadesTest {

    private static final Instant AHORA = Instant.parse("2026-11-13T12:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    @Mock
    private EntregaRepository entregas;

    @Mock
    private EvaluacionRepository evaluaciones;

    @Mock
    private ObservacionRepository observaciones;

    @Mock
    private ProyectoGradoRepository proyectos;

    @Mock
    private FechaLimiteEtapaRepository fechas;

    @Mock
    private PermisosEntidadService permisos;

    @Mock
    private ProyectoAccessService acceso;

    @Mock
    private VisibilidadProyectoService visibilidad;

    @Mock
    private AvanceService avance;

    private EntregaServiceImpl entregaService;
    private EvaluacionServiceImpl evaluacionService;
    private ObservacionServiceImpl observacionService;
    private ProyectoGradoServiceImpl proyectoService;
    private ProyectoGrado proyecto;
    private User usuario;

    @BeforeEach
    void preparar() {
        entregaService = new EntregaServiceImpl(
            entregas,
            new EntregaMapperImpl(),
            fechas,
            observaciones,
            acceso,
            permisos,
            visibilidad,
            avance,
            RELOJ,
            org.springframework.util.unit.DataSize.ofMegabytes(10)
        );
        evaluacionService = new EvaluacionServiceImpl(evaluaciones, new EvaluacionMapperImpl(), acceso, permisos, avance, RELOJ);
        observacionService = new ObservacionServiceImpl(
            observaciones,
            entregas,
            new ObservacionMapperImpl(),
            acceso,
            permisos,
            avance,
            RELOJ
        );
        proyectoService = new ProyectoGradoServiceImpl(
            proyectos,
            new ProyectoGradoMapperImpl(),
            acceso,
            permisos,
            visibilidad,
            avance,
            RELOJ
        );
        proyecto = new ProyectoGrado()
            .id(1L)
            .titulo("Proyecto de prueba")
            .resumen("Resumen")
            .programa("Ingeniería")
            .etapaActual(Etapa.PROPUESTA)
            .estado(EstadoProyecto.ACTIVO)
            .porcentajeAvance(0)
            .fechaRegistro(Instant.EPOCH);
        usuario = new User();
        usuario.setId(10L);
        usuario.setLogin("estudiante");
    }

    private EntregaDTO nuevaEntrega() {
        when(permisos.proyecto(1L)).thenReturn(proyecto);
        EntregaDTO dto = new EntregaDTO();
        dto.setTitulo("Documento");
        dto.setUrlDocumento("https://example.org/documento.pdf");
        ProyectoGradoDTO relacion = new ProyectoGradoDTO();
        relacion.setId(1L);
        dto.setProyecto(relacion);
        return dto;
    }

    private Entrega entregaExistente() {
        Entrega entrega = new Entrega()
            .id(2L)
            .proyecto(proyecto)
            .etapa(Etapa.PROPUESTA)
            .titulo("Documento")
            .estado(EstadoEntrega.ENVIADA)
            .version(1)
            .fechaEntrega(Instant.EPOCH)
            .autor(usuario);
        when(entregas.findById(2L)).thenReturn(Optional.of(entrega));
        return entrega;
    }

    private void verificarRegla(String codigo, Runnable accion) {
        assertThatThrownBy(accion::run).isInstanceOfSatisfying(ReglaNegocioException.class, ex -> {
            assertThat(ex.getCodigo()).isEqualTo(codigo);
            assertThat(ex.getStatusCode().value()).isEqualTo(409);
        });
        verifyNoInteractions(avance);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoProyecto.class, names = "ACTIVO", mode = EnumSource.Mode.EXCLUDE)
    void noAdmiteEntregasDeProyectosInactivos(EstadoProyecto estado) {
        EntregaDTO dto = nuevaEntrega();
        proyecto.setEstado(estado);
        verificarRegla("PROYECTO_NO_ACTIVO", () -> entregaService.save(dto));
        verify(entregas, never()).save(any());
    }

    @Test
    void noAdmiteEntregasEnFinalizado() {
        EntregaDTO dto = nuevaEntrega();
        proyecto.setEtapaActual(Etapa.FINALIZADO);
        verificarRegla("PROYECTO_NO_ACTIVO", () -> entregaService.save(dto));
    }

    @Test
    void fechaVencidaUsaElRelojInyectado() {
        EntregaDTO dto = nuevaEntrega();
        when(fechas.findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(1L, Etapa.PROPUESTA)).thenReturn(
            Optional.of(new FechaLimiteEtapa().fechaLimite(AHORA.minusNanos(1)))
        );
        verificarRegla("FECHA_LIMITE_VENCIDA", () -> entregaService.save(dto));
    }

    @Test
    void noAdmiteDosEntregasEnCurso() {
        EntregaDTO dto = nuevaEntrega();
        when(
            entregas.existsByProyectoIdAndEtapaAndEstadoIn(1L, Etapa.PROPUESTA, List.of(EstadoEntrega.ENVIADA, EstadoEntrega.EN_REVISION))
        ).thenReturn(true);
        verificarRegla("ENTREGA_EN_CURSO", () -> entregaService.save(dto));
    }

    @Test
    void noAdmiteEntregasSiLaEtapaEstaAprobada() {
        EntregaDTO dto = nuevaEntrega();
        when(entregas.existsByProyectoIdAndEtapaAndEstado(1L, Etapa.PROPUESTA, EstadoEntrega.APROBADA)).thenReturn(true);
        verificarRegla("ETAPA_YA_APROBADA", () -> entregaService.save(dto));
    }

    @Test
    void fuerzaCamposDeEntregaYAdmiteLaFechaLimiteExacta() {
        EntregaDTO dto = nuevaEntrega();
        dto.setEtapa(Etapa.FINALIZADO);
        dto.setEstado(EstadoEntrega.APROBADA);
        dto.setVersion(99);
        dto.setFechaEntrega(Instant.EPOCH);
        when(fechas.findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(1L, Etapa.PROPUESTA)).thenReturn(
            Optional.of(new FechaLimiteEtapa().fechaLimite(AHORA))
        );
        when(entregas.findMaxVersion(1L, Etapa.PROPUESTA)).thenReturn(4);
        when(permisos.usuarioActual()).thenReturn(usuario);
        when(entregas.save(any())).thenAnswer(inv -> inv.getArgument(0));
        EntregaDTO resultado = entregaService.save(dto);
        assertThat(resultado.getEtapa()).isEqualTo(Etapa.PROPUESTA);
        assertThat(resultado.getEstado()).isEqualTo(EstadoEntrega.ENVIADA);
        assertThat(resultado.getVersion()).isEqualTo(5);
        assertThat(resultado.getFechaEntrega()).isEqualTo(AHORA);
        assertThat(resultado.getAutor().getId()).isEqualTo(usuario.getId());
        verify(permisos).estudiante(1L);
        verify(avance).recalcular(1L);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void estudianteEditaContenidoSinCambiarCamposForzados(boolean parcial) {
        Entrega entrega = entregaExistente();
        EntregaDTO dto = new EntregaDTO();
        dto.setId(2L);
        dto.setTitulo("Título editado");
        dto.setEtapa(Etapa.FINALIZADO);
        dto.setEstado(EstadoEntrega.APROBADA);
        dto.setVersion(99);
        dto.setFechaEntrega(AHORA);
        when(entregas.save(any())).thenAnswer(inv -> inv.getArgument(0));
        if (parcial) entregaService.partialUpdate(dto);
        else entregaService.update(dto);
        assertThat(entrega.getTitulo()).isEqualTo("Título editado");
        assertThat(entrega.getEstado()).isEqualTo(EstadoEntrega.ENVIADA);
        assertThat(entrega.getEtapa()).isEqualTo(Etapa.PROPUESTA);
        assertThat(entrega.getVersion()).isEqualTo(1);
        assertThat(entrega.getFechaEntrega()).isEqualTo(Instant.EPOCH);
        assertThat(entrega.getAutor()).isSameAs(usuario);
        assertThat(entrega.getProyecto()).isSameAs(proyecto);
        verify(permisos).estudiante(1L);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEntrega.class, names = "ENVIADA", mode = EnumSource.Mode.EXCLUDE)
    void estudianteNoEditaDespuesDeEnviarARevision(EstadoEntrega estado) {
        entregaExistente().setEstado(estado);
        EntregaDTO dto = new EntregaDTO();
        dto.setId(2L);
        assertThatThrownBy(() -> entregaService.partialUpdate(dto)).isInstanceOf(AccessDeniedException.class);
        verify(entregas, never()).save(any());
    }

    @Test
    void noApruebaUnaEntregaConCorreccionesPendientes() {
        entregaExistente();
        when(observaciones.countByEntregaIdAndTipoAndResueltaFalse(2L, TipoObservacion.CORRECCION_REQUERIDA)).thenReturn(1L);
        verificarRegla("CORRECCIONES_PENDIENTES", () -> entregaService.revisar(2L, new RevisionEntregaDTO(EstadoEntrega.APROBADA)));
    }

    @Test
    void revisionDeOtraEtapaEsProhibida() {
        entregaExistente();
        proyecto.setEtapaActual(Etapa.ANTEPROYECTO);
        assertThatThrownBy(() -> entregaService.revisar(2L, new RevisionEntregaDTO(EstadoEntrega.APROBADA))).isInstanceOf(
            AccessDeniedException.class
        );
        verify(entregas, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEntrega.class, names = "ENVIADA", mode = EnumSource.Mode.EXCLUDE)
    void permiteLosCuatroEstadosDeRevision(EstadoEntrega estado) {
        Entrega entrega = entregaExistente();
        when(entregas.save(any())).thenAnswer(inv -> inv.getArgument(0));
        entregaService.revisar(2L, new RevisionEntregaDTO(estado));
        assertThat(entrega.getEstado()).isEqualTo(estado);
        verify(permisos).tutorOAdmin(1L);
        verify(avance).recalcular(1L);
    }

    private EvaluacionDTO nuevaEvaluacion() {
        when(permisos.proyecto(1L)).thenReturn(proyecto);
        EvaluacionDTO dto = new EvaluacionDTO();
        ProyectoGradoDTO relacion = new ProyectoGradoDTO();
        relacion.setId(1L);
        dto.setProyecto(relacion);
        dto.setCalificacion(new BigDecimal("4.0"));
        dto.setConcepto(Concepto.APROBADO);
        return dto;
    }

    @ParameterizedTest
    @EnumSource(value = Etapa.class, names = { "PROPUESTA", "DESARROLLO", "FINALIZADO" })
    void rechazaEvaluacionDeEtapaNoEvaluable(Etapa etapa) {
        EvaluacionDTO dto = nuevaEvaluacion();
        proyecto.setEtapaActual(etapa);
        verificarRegla("ETAPA_NO_EVALUABLE", () -> evaluacionService.save(dto));
    }

    @Test
    void rechazaEvaluacionDuplicada() {
        EvaluacionDTO dto = nuevaEvaluacion();
        proyecto.setEtapaActual(Etapa.ANTEPROYECTO);
        when(permisos.usuarioActual()).thenReturn(usuario);
        when(evaluaciones.existsByProyectoIdAndEtapaAndJuradoLogin(1L, Etapa.ANTEPROYECTO, "estudiante")).thenReturn(true);
        verificarRegla("EVALUACION_DUPLICADA", () -> evaluacionService.save(dto));
    }

    @ParameterizedTest
    @CsvSource({ "2.9,APROBADO", "2.9,APROBADO_CON_MODIFICACIONES", "3.0,REPROBADO", "5.0,REPROBADO" })
    void rechazaLasDosDireccionesDeConceptoIncoherente(BigDecimal nota, Concepto concepto) {
        EvaluacionDTO dto = nuevaEvaluacion();
        proyecto.setEtapaActual(Etapa.ANTEPROYECTO);
        dto.setCalificacion(nota);
        dto.setConcepto(concepto);
        when(permisos.usuarioActual()).thenReturn(usuario);
        verificarRegla("CONCEPTO_INCOHERENTE", () -> evaluacionService.save(dto));
        verify(evaluaciones, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({ "ANTEPROYECTO,2.9,REPROBADO", "SUSTENTACION,3.0,APROBADO", "ANTEPROYECTO,3.0,APROBADO_CON_MODIFICACIONES" })
    void evaluacionFuerzaEtapaJuradoYFecha(Etapa etapa, BigDecimal nota, Concepto concepto) {
        EvaluacionDTO dto = nuevaEvaluacion();
        proyecto.setEtapaActual(etapa);
        dto.setCalificacion(nota);
        dto.setConcepto(concepto);
        dto.setEtapa(Etapa.FINALIZADO);
        dto.setFechaEvaluacion(Instant.EPOCH);
        when(permisos.usuarioActual()).thenReturn(usuario);
        when(evaluaciones.save(any())).thenAnswer(inv -> inv.getArgument(0));
        EvaluacionDTO resultado = evaluacionService.save(dto);
        assertThat(resultado.getEtapa()).isEqualTo(etapa);
        assertThat(resultado.getJurado().getId()).isEqualTo(usuario.getId());
        assertThat(resultado.getFechaEvaluacion()).isEqualTo(AHORA);
        verify(permisos).jurado(1L);
        verify(avance).recalcular(1L);
    }

    @Test
    void observacionFuerzaAutorFechaYResueltaYCambiaEstadoDeEntrega() {
        Entrega entrega = entregaExistente();
        ObservacionDTO dto = new ObservacionDTO();
        EntregaDTO relacion = new EntregaDTO();
        relacion.setId(2L);
        dto.setEntrega(relacion);
        dto.setContenido("Corregir referencias");
        dto.setTipo(TipoObservacion.CORRECCION_REQUERIDA);
        dto.setResuelta(true);
        dto.setFechaCreacion(Instant.EPOCH);
        when(permisos.usuarioActual()).thenReturn(usuario);
        when(observaciones.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ObservacionDTO resultado = observacionService.save(dto);
        assertThat(resultado.getResuelta()).isFalse();
        assertThat(resultado.getFechaCreacion()).isEqualTo(AHORA);
        assertThat(resultado.getAutor().getId()).isEqualTo(usuario.getId());
        assertThat(entrega.getEstado()).isEqualTo(EstadoEntrega.CON_OBSERVACIONES);
        verify(permisos).tutorOAdmin(1L);
        verify(avance).recalcular(1L);
    }

    @Test
    void resolverConservaLosDatosOriginalesDeLaObservacion() {
        Observacion observacion = new Observacion()
            .id(3L)
            .entrega(new Entrega().proyecto(proyecto))
            .tipo(TipoObservacion.CORRECCION_REQUERIDA)
            .resuelta(false)
            .autor(usuario)
            .fechaCreacion(Instant.EPOCH);
        when(observaciones.findById(3L)).thenReturn(Optional.of(observacion));
        when(observaciones.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ObservacionDTO resultado = observacionService.resolver(3L).orElseThrow();
        assertThat(resultado.getResuelta()).isTrue();
        assertThat(resultado.getFechaCreacion()).isEqualTo(Instant.EPOCH);
        verify(permisos).tutorOAdmin(1L);
        verify(avance).recalcular(1L);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void editarObservacionNoPermiteResolverlaNiMoverla(boolean parcial) {
        Entrega entrega = new Entrega().id(2L).proyecto(proyecto).estado(EstadoEntrega.ENVIADA);
        Observacion observacion = new Observacion()
            .id(3L)
            .entrega(entrega)
            .contenido("Texto original")
            .tipo(TipoObservacion.GENERAL)
            .resuelta(false)
            .autor(usuario)
            .fechaCreacion(Instant.EPOCH);
        when(observaciones.findById(3L)).thenReturn(Optional.of(observacion));
        when(observaciones.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ObservacionDTO dto = new ObservacionDTO();
        dto.setId(3L);
        dto.setContenido("Corregir referencias");
        dto.setTipo(TipoObservacion.CORRECCION_REQUERIDA);
        dto.setResuelta(true);
        dto.setFechaCreacion(AHORA);
        dto.setAutor(new UserDTO());
        dto.setEntrega(new EntregaDTO());
        if (parcial) observacionService.partialUpdate(dto);
        else observacionService.update(dto);
        assertThat(observacion.getContenido()).isEqualTo("Corregir referencias");
        assertThat(observacion.getResuelta()).isFalse();
        assertThat(observacion.getAutor()).isSameAs(usuario);
        assertThat(observacion.getFechaCreacion()).isEqualTo(Instant.EPOCH);
        assertThat(observacion.getEntrega()).isSameAs(entrega);
        assertThat(entrega.getEstado()).isEqualTo(EstadoEntrega.CON_OBSERVACIONES);
        verify(permisos).tutorOAdmin(1L);
        verify(avance).recalcular(1L);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void adminEditaEvaluacionSinCambiarProyectoJuradoEtapaOFecha(boolean parcial) {
        Evaluacion evaluacion = new Evaluacion()
            .id(3L)
            .proyecto(proyecto)
            .jurado(usuario)
            .etapa(Etapa.ANTEPROYECTO)
            .fechaEvaluacion(Instant.EPOCH)
            .calificacion(BigDecimal.ONE)
            .concepto(Concepto.REPROBADO);
        when(evaluaciones.findById(3L)).thenReturn(Optional.of(evaluacion));
        when(evaluaciones.save(any())).thenAnswer(inv -> inv.getArgument(0));
        EvaluacionDTO dto = new EvaluacionDTO();
        dto.setId(3L);
        dto.setCalificacion(new BigDecimal("4.0"));
        dto.setConcepto(Concepto.APROBADO);
        dto.setEtapa(Etapa.FINALIZADO);
        dto.setFechaEvaluacion(AHORA);
        dto.setProyecto(new ProyectoGradoDTO());
        dto.setJurado(new UserDTO());
        if (parcial) evaluacionService.partialUpdate(dto);
        else evaluacionService.update(dto);
        assertThat(evaluacion.getCalificacion()).isEqualByComparingTo("4.0");
        assertThat(evaluacion.getConcepto()).isEqualTo(Concepto.APROBADO);
        assertThat(evaluacion.getProyecto()).isSameAs(proyecto);
        assertThat(evaluacion.getJurado()).isSameAs(usuario);
        assertThat(evaluacion.getEtapa()).isEqualTo(Etapa.ANTEPROYECTO);
        assertThat(evaluacion.getFechaEvaluacion()).isEqualTo(Instant.EPOCH);
        verify(permisos).admin();
        verify(avance).recalcular(1L);
    }

    @Test
    void estudianteCreaPropuestaSinPoderAsignarPersonal() {
        ProyectoGradoDTO dto = new ProyectoGradoMapperImpl().toDto(proyecto);
        UserDTO ajeno = new UserDTO();
        ajeno.setId(99L);
        dto.setTutor(ajeno);
        dto.setJurados(Set.of(ajeno));
        dto.setEstudiantes(Set.of(ajeno));
        dto.setEtapaActual(Etapa.FINALIZADO);
        dto.setEstado(EstadoProyecto.APROBADO);
        dto.setPorcentajeAvance(100);
        when(permisos.usuarioActual()).thenReturn(usuario);
        when(proyectos.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ProyectoGradoDTO resultado = proyectoService.save(dto);
        assertThat(resultado.getEtapaActual()).isEqualTo(Etapa.PROPUESTA);
        assertThat(resultado.getEstado()).isEqualTo(EstadoProyecto.ACTIVO);
        assertThat(resultado.getPorcentajeAvance()).isZero();
        assertThat(resultado.getFechaRegistro()).isEqualTo(AHORA);
        assertThat(resultado.getFechaActualizacion()).isEqualTo(AHORA);
        assertThat(resultado.getTutor()).isNull();
        assertThat(resultado.getJurados()).isEmpty();
        assertThat(resultado.getEstudiantes()).extracting(UserDTO::getId).containsExactly(usuario.getId());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void estudianteSoloCambiaTituloYResumenSinAlterarElWorkflow(boolean parcial) {
        proyecto.setTutor(usuario);
        proyecto.addJurado(usuario);
        proyecto.addEstudiante(usuario);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(proyectos.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ProyectoGradoDTO dto = new ProyectoGradoDTO();
        dto.setId(1L);
        dto.setTitulo("Título cambiado");
        dto.setResumen("Nuevo resumen");
        dto.setPrograma("Otro programa");
        dto.setEtapaActual(Etapa.FINALIZADO);
        dto.setEstado(EstadoProyecto.CANCELADO);
        dto.setFechaRegistro(AHORA);
        dto.setPorcentajeAvance(100);
        dto.setEstudiantes(Set.of());
        dto.setJurados(Set.of());
        if (parcial) proyectoService.partialUpdate(dto);
        else proyectoService.update(dto);
        assertThat(proyecto.getTitulo()).isEqualTo("Título cambiado");
        assertThat(proyecto.getResumen()).isEqualTo("Nuevo resumen");
        assertThat(proyecto.getPrograma()).isEqualTo("Ingeniería");
        assertThat(proyecto.getEtapaActual()).isEqualTo(Etapa.PROPUESTA);
        assertThat(proyecto.getEstado()).isEqualTo(EstadoProyecto.ACTIVO);
        assertThat(proyecto.getPorcentajeAvance()).isZero();
        assertThat(proyecto.getFechaRegistro()).isEqualTo(Instant.EPOCH);
        assertThat(proyecto.getTutor()).isSameAs(usuario);
        assertThat(proyecto.getJurados()).containsExactly(usuario);
        assertThat(proyecto.getEstudiantes()).containsExactly(usuario);
        verify(permisos).estudiante(1L);
        verify(avance).recalcular(1L);
    }

    @Test
    void estudianteNoEditaProyectoFueraDePropuesta() {
        proyecto.setEtapaActual(Etapa.DESARROLLO);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        ProyectoGradoDTO dto = new ProyectoGradoDTO();
        dto.setId(1L);
        assertThatThrownBy(() -> proyectoService.partialUpdate(dto)).isInstanceOf(AccessDeniedException.class);
        verify(proyectos, never()).save(any());
    }

    @Test
    void consultaObservacionesVerificaVisibilidadAntesDeLeerlasYConservaElOrden() {
        Entrega entrega = entregaExistente();
        var primera = new Observacion().id(3L).entrega(entrega).contenido("Primera").fechaCreacion(Instant.EPOCH);
        var segunda = new Observacion().id(4L).entrega(entrega).contenido("Segunda").fechaCreacion(AHORA);
        when(observaciones.findByEntregaIdOrderByFechaCreacionAsc(2L)).thenReturn(List.of(primera, segunda));
        assertThat(observacionService.findByEntrega(2L).orElseThrow()).extracting(ObservacionDTO::getId).containsExactly(3L, 4L);
        var orden = inOrder(acceso, observaciones);
        orden.verify(acceso).verificarPuedeVer(1L);
        orden.verify(observaciones).findByEntregaIdOrderByFechaCreacionAsc(2L);
    }

    @Test
    void consultaObservacionesDeProyectoAjenoEsProhibida() {
        entregaExistente();
        doThrow(new AccessDeniedException("Proyecto ajeno")).when(acceso).verificarPuedeVer(1L);
        assertThatThrownBy(() -> observacionService.findByEntrega(2L)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(observaciones);
    }

    @Test
    void consultaObservacionesDistingueEntregaInexistenteDeListaVacia() {
        assertThat(observacionService.findByEntrega(99L)).isEmpty();
        entregaExistente();
        assertThat(observacionService.findByEntrega(2L).orElseThrow()).isEmpty();
        verify(acceso).verificarPuedeVer(1L);
    }
}
