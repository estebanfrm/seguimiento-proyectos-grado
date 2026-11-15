package co.edu.sgpg.service.workflow;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.domain.HistorialEtapa;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.*;
import co.edu.sgpg.service.AvanceService;
import co.edu.sgpg.service.ProyectoAccessService;
import co.edu.sgpg.service.ReglaNegocioException;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import co.edu.sgpg.service.dto.workflow.RequisitosTransicionDTO;
import co.edu.sgpg.service.mapper.ProyectoGradoMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MotorEtapasServiceTest {

    private static final Instant AHORA = Instant.parse("2026-11-13T15:00:00Z");

    @Mock
    private ProyectoGradoRepository proyectos;

    @Mock
    private ProyectoAccessService acceso;

    @Mock
    private EvaluadorRequisitos evaluador;

    @Mock
    private AvanceService avance;

    @Mock
    private HistorialEtapaRepository historial;

    @Mock
    private UserRepository usuarios;

    @Mock
    private EntregaRepository entregas;

    @Mock
    private ObservacionRepository observaciones;

    @Mock
    private EvaluacionRepository evaluaciones;

    @Mock
    private FechaLimiteEtapaRepository fechas;

    private MotorEtapasService service;
    private ProyectoGrado proyecto;
    private User usuario;

    @BeforeEach
    void preparar() {
        service = new MotorEtapasService(
            proyectos,
            acceso,
            evaluador,
            new Requisitos(entregas, observaciones, evaluaciones, fechas),
            avance,
            historial,
            usuarios,
            Mappers.getMapper(ProyectoGradoMapper.class),
            Clock.fixed(AHORA, ZoneOffset.UTC)
        );
        proyecto = new ProyectoGrado().id(1L).etapaActual(Etapa.PROPUESTA).estado(EstadoProyecto.ACTIVO).porcentajeAvance(0);
        usuario = new User();
        usuario.setId(2L);
        usuario.setLogin("tutor");
    }

    private void cargarProyecto() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
    }

    private void prepararMutacion(int porcentaje) {
        when(acceso.loginActual()).thenReturn(Optional.of("tutor"));
        when(usuarios.findOneByLogin("tutor")).thenReturn(Optional.of(usuario));
        when(avance.recalcular(1L)).thenReturn(porcentaje);
    }

    private void transicion(Etapa destino, RequisitoDTO... requisitos) {
        when(evaluador.evaluar(proyecto)).thenReturn(
            new RequisitosTransicionDTO(1L, proyecto.getEtapaActual(), destino, true, List.of(requisitos))
        );
    }

    @Test
    void avanzaPersisteFechaHistorialUsuarioYPorcentaje() {
        cargarProyecto();
        prepararMutacion(20);
        transicion(Etapa.ANTEPROYECTO, new RequisitoDTO("ENTREGA_APROBADA", "Entrega", true));
        var dto = service.avanzar(1L, "Propuesta aceptada");
        assertThat(dto.getEtapaActual()).isEqualTo(Etapa.ANTEPROYECTO);
        assertThat(dto.getEstado()).isEqualTo(EstadoProyecto.ACTIVO);
        assertThat(dto.getPorcentajeAvance()).isEqualTo(20);
        assertThat(dto.getFechaActualizacion()).isEqualTo(AHORA);
        var registro = ArgumentCaptor.forClass(HistorialEtapa.class);
        verify(historial).save(registro.capture());
        assertThat(registro.getValue().getEtapaOrigen()).isEqualTo(Etapa.PROPUESTA);
        assertThat(registro.getValue().getEtapaDestino()).isEqualTo(Etapa.ANTEPROYECTO);
        assertThat(registro.getValue().getFecha()).isEqualTo(AHORA);
        assertThat(registro.getValue().getUsuario()).isSameAs(usuario);
        assertThat(registro.getValue().getProyecto()).isSameAs(proyecto);
        assertThat(registro.getValue().getComentario()).isEqualTo("Propuesta aceptada");
        verify(acceso).verificarTutorOAdmin(1L);
        verify(proyectos).save(proyecto);
        verify(avance).recalcular(1L);
    }

    @Test
    void requisitosPendientesIncluyeCodigosSinMutaciones() {
        cargarProyecto();
        transicion(
            Etapa.ANTEPROYECTO,
            new RequisitoDTO("TUTOR_ASIGNADO", "Tutor", false),
            new RequisitoDTO("ESTUDIANTES_ASIGNADOS", "Estudiante", true),
            new RequisitoDTO("ENTREGA_APROBADA", "Entrega", false)
        );
        assertThatThrownBy(() -> service.avanzar(1L, null)).isInstanceOfSatisfying(ReglaNegocioException.class, e -> {
            assertThat(e.getCodigo()).isEqualTo("REQUISITOS_PENDIENTES");
            assertThat(e.getPendientes()).containsExactly("TUTOR_ASIGNADO", "ENTREGA_APROBADA");
        });
        assertThat(proyecto.getEtapaActual()).isEqualTo(Etapa.PROPUESTA);
        verify(proyectos, never()).save(any());
        verifyNoInteractions(historial, avance, usuarios);
    }

    @ParameterizedTest
    @CsvSource({ "3,APROBADO,APROBADO", "2.99,REPROBADO,REPROBADO", "5,REPROBADO,REPROBADO", "2.99,APROBADO,REPROBADO" })
    void finalizaConResultadoAcademico(String calificacion, Concepto concepto, EstadoProyecto esperado) {
        proyecto.setEtapaActual(Etapa.SUSTENTACION);
        cargarProyecto();
        prepararMutacion(100);
        transicion(Etapa.FINALIZADO, new RequisitoDTO("EVALUACIONES_COMPLETAS", "Evaluaciones", true));
        when(evaluaciones.findByProyectoIdAndEtapa(1L, Etapa.SUSTENTACION)).thenReturn(
            List.of(new Evaluacion().calificacion(new BigDecimal(calificacion)).concepto(concepto))
        );
        var dto = service.avanzar(1L, null);
        assertThat(dto.getEstado()).isEqualTo(esperado);
        assertThat(dto.getEtapaActual()).isEqualTo(Etapa.FINALIZADO);
        assertThat(dto.getPorcentajeAvance()).isEqualTo(100);
    }

    @Test
    void cancelaConHistorialEnLaMismaEtapa() {
        cargarProyecto();
        prepararMutacion(4);
        when(acceso.esAdmin()).thenReturn(true);
        var dto = service.cancelar(1L, "Retiro voluntario");
        assertThat(dto.getEstado()).isEqualTo(EstadoProyecto.CANCELADO);
        assertThat(dto.getEtapaActual()).isEqualTo(Etapa.PROPUESTA);
        assertThat(dto.getPorcentajeAvance()).isEqualTo(4);
        assertThat(dto.getFechaActualizacion()).isEqualTo(AHORA);
        verify(historial).save(
            argThat(
                h ->
                    h.getEtapaOrigen() == Etapa.PROPUESTA &&
                    h.getEtapaDestino() == Etapa.PROPUESTA &&
                    h.getComentario().equals("Retiro voluntario") &&
                    h.getUsuario() == usuario &&
                    h.getFecha().equals(AHORA)
            )
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "\t\n" })
    void cancelarExigeComentario(String comentario) {
        cargarProyecto();
        when(acceso.esAdmin()).thenReturn(true);
        assertCodigo(() -> service.cancelar(1L, comentario), "COMENTARIO_OBLIGATORIO");
        verifyNoInteractions(historial, avance, usuarios);
    }

    @Test
    void tutorNoPuedeCancelar() {
        cargarProyecto();
        assertThatThrownBy(() -> service.cancelar(1L, "Cancelación")).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(historial, avance);
    }

    @Test
    void avanzarExigePermisoAntesDeEvaluar() {
        cargarProyecto();
        doThrow(new AccessDeniedException("Sin permiso")).when(acceso).verificarTutorOAdmin(1L);
        assertThatThrownBy(() -> service.avanzar(1L, null)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(evaluador, historial, avance);
    }

    @Test
    void consultaRequisitosExigeVisibilidad() {
        cargarProyecto();
        doThrow(new AccessDeniedException("Sin acceso")).when(acceso).verificarPuedeVer(1L);
        assertThatThrownBy(() -> service.requisitos(1L)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(evaluador);
    }

    @Test
    void consultaRequisitosSinMutar() {
        cargarProyecto();
        transicion(Etapa.ANTEPROYECTO);
        assertThat(service.requisitos(1L).etapaSiguiente()).isEqualTo(Etapa.ANTEPROYECTO);
        verify(acceso).verificarPuedeVer(1L);
        verifyNoInteractions(historial, avance);
    }

    @Test
    void proyectoNoActivoYEtapaFinalSeRechazan() {
        cargarProyecto();
        proyecto.setEstado(EstadoProyecto.CANCELADO);
        assertCodigo(() -> service.avanzar(1L, null), "PROYECTO_NO_ACTIVO");
        proyecto.setEstado(EstadoProyecto.ACTIVO);
        proyecto.setEtapaActual(Etapa.FINALIZADO);
        assertCodigo(() -> service.avanzar(1L, null), "ETAPA_FINAL");
        verifyNoInteractions(evaluador, historial, avance);
    }

    @Test
    void usuarioInexistenteNoProduceMutaciones() {
        cargarProyecto();
        transicion(Etapa.ANTEPROYECTO);
        assertThatThrownBy(() -> service.avanzar(1L, null)).isInstanceOf(AccessDeniedException.class);
        assertThat(proyecto.getEtapaActual()).isEqualTo(Etapa.PROPUESTA);
        verifyNoInteractions(historial, avance);
    }

    @Test
    void proyectoInexistenteDevuelve404EnTodasLasOperaciones() {
        assert404(() -> service.requisitos(99L));
        assert404(() -> service.avanzar(99L, null));
        assert404(() -> service.cancelar(99L, "Motivo"));
        verifyNoInteractions(acceso, evaluador, historial, avance);
    }

    private void assert404(org.assertj.core.api.ThrowableAssert.ThrowingCallable accion) {
        assertThatThrownBy(accion).isInstanceOfSatisfying(ResponseStatusException.class, e ->
            assertThat(e.getStatusCode().value()).isEqualTo(404)
        );
    }

    private void assertCodigo(org.assertj.core.api.ThrowableAssert.ThrowingCallable accion, String codigo) {
        assertThatThrownBy(accion).isInstanceOfSatisfying(ReglaNegocioException.class, e -> assertThat(e.getCodigo()).isEqualTo(codigo));
    }
}
