package co.edu.sgpg.service.workflow;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.domain.FechaLimiteEtapa;
import co.edu.sgpg.domain.HistorialEtapa;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Concepto;
import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.*;
import co.edu.sgpg.service.ProyectoAccessService;
import co.edu.sgpg.service.dto.workflow.RequisitosTransicionDTO;
import co.edu.sgpg.service.mapper.EvaluacionMapper;
import co.edu.sgpg.service.mapper.HistorialEtapaMapper;
import co.edu.sgpg.service.mapper.ProyectoGradoMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SeguimientoServiceTest {

    private static final Instant AHORA = Instant.parse("2026-11-13T23:30:00Z");

    @Mock
    private ProyectoGradoRepository proyectos;

    @Mock
    private ProyectoAccessService acceso;

    @Mock
    private EntregaRepository entregas;

    @Mock
    private ObservacionRepository observaciones;

    @Mock
    private EvaluacionRepository evaluaciones;

    @Mock
    private FechaLimiteEtapaRepository fechas;

    @Mock
    private HistorialEtapaRepository historial;

    @Mock
    private EvaluadorRequisitos evaluador;

    private SeguimientoService service;
    private ProyectoGrado proyecto;

    @BeforeEach
    void preparar() {
        service = new SeguimientoService(
            proyectos,
            acceso,
            entregas,
            observaciones,
            evaluaciones,
            fechas,
            historial,
            Mappers.getMapper(ProyectoGradoMapper.class),
            Mappers.getMapper(EvaluacionMapper.class),
            Mappers.getMapper(HistorialEtapaMapper.class),
            evaluador,
            Clock.fixed(AHORA, ZoneId.of("America/Bogota"))
        );
        proyecto = new ProyectoGrado()
            .id(1L)
            .titulo("Proyecto de prueba")
            .etapaActual(Etapa.DESARROLLO)
            .estado(EstadoProyecto.ACTIVO)
            .porcentajeAvance(48);
    }

    @Test
    void agrupaEnOrdenConLaFechaMasRecienteDiasYObservacionesSinResolver() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        Instant ayer = Instant.parse("2026-11-12T23:59:00Z");
        Instant manana = Instant.parse("2026-11-15T02:00:00Z"); // 14 de noviembre en el reloj de Bogotá.
        when(fechas.findByProyectoIdOrderByFechaLimiteAsc(1L)).thenReturn(
            List.of(
                new FechaLimiteEtapa().etapa(Etapa.DESARROLLO).fechaLimite(ayer),
                new FechaLimiteEtapa().etapa(Etapa.PROPUESTA).fechaLimite(ayer),
                new FechaLimiteEtapa().etapa(Etapa.DESARROLLO).fechaLimite(manana)
            )
        );
        when(entregas.findByProyectoIdOrderByEtapaAscVersionAsc(1L)).thenReturn(
            List.of(
                new Entrega()
                    .id(10L)
                    .etapa(Etapa.PROPUESTA)
                    .titulo("Propuesta")
                    .version(1)
                    .estado(EstadoEntrega.APROBADA)
                    .fechaEntrega(ayer),
                new Entrega()
                    .id(11L)
                    .etapa(Etapa.DESARROLLO)
                    .titulo("Documento final")
                    .version(2)
                    .estado(EstadoEntrega.CON_OBSERVACIONES)
                    .fechaEntrega(AHORA)
            )
        );
        when(observaciones.countByEntregaIdAndResueltaFalse(10L)).thenReturn(0L);
        when(observaciones.countByEntregaIdAndResueltaFalse(11L)).thenReturn(2L);
        when(evaluaciones.findByProyectoIdOrderByFechaEvaluacionAsc(1L)).thenReturn(
            List.of(
                new Evaluacion()
                    .id(20L)
                    .etapa(Etapa.ANTEPROYECTO)
                    .calificacion(new BigDecimal("4.5"))
                    .concepto(Concepto.APROBADO)
                    .fechaEvaluacion(ayer)
            )
        );
        when(historial.findByProyectoIdOrderByFechaAsc(1L)).thenReturn(
            List.of(
                new HistorialEtapa().id(30L).etapaOrigen(Etapa.PROPUESTA).etapaDestino(Etapa.ANTEPROYECTO).fecha(ayer),
                new HistorialEtapa().id(31L).etapaOrigen(Etapa.ANTEPROYECTO).etapaDestino(Etapa.DESARROLLO).fecha(AHORA)
            )
        );
        var requisitos = new RequisitosTransicionDTO(1L, Etapa.DESARROLLO, Etapa.SUSTENTACION, false, List.of());
        when(evaluador.evaluar(proyecto)).thenReturn(requisitos);

        var resultado = service.obtener(1L);

        assertThat(resultado.proyecto().getId()).isEqualTo(1L);
        assertThat(resultado.porcentajeAvance()).isEqualTo(48);
        assertThat(resultado.etapas())
            .extracting(e -> e.etapa())
            .containsExactly(Etapa.values());
        assertThat(resultado.etapas())
            .extracting(e -> e.estado())
            .containsExactly("COMPLETADA", "COMPLETADA", "ACTUAL", "PENDIENTE", "PENDIENTE");
        var propuesta = resultado.etapas().get(0);
        assertThat(propuesta.vencida()).isTrue();
        assertThat(propuesta.diasRestantes()).isEqualTo(-1L);
        assertThat(propuesta.entregas().getFirst().observacionesPendientes()).isZero();
        var desarrollo = resultado.etapas().get(2);
        assertThat(desarrollo.fechaLimite()).isEqualTo(manana);
        assertThat(desarrollo.vencida()).isFalse();
        assertThat(desarrollo.diasRestantes()).isEqualTo(1L);
        var entrega = desarrollo.entregas().getFirst();
        assertThat(entrega.id()).isEqualTo(11L);
        assertThat(entrega.titulo()).isEqualTo("Documento final");
        assertThat(entrega.version()).isEqualTo(2);
        assertThat(entrega.estado()).isEqualTo(EstadoEntrega.CON_OBSERVACIONES);
        assertThat(entrega.fechaEntrega()).isEqualTo(AHORA);
        assertThat(entrega.observacionesPendientes()).isEqualTo(2);
        assertThat(desarrollo.evaluaciones()).isEmpty();
        assertThat(resultado.etapas().get(1).evaluaciones().getFirst().getCalificacion()).isEqualByComparingTo("4.5");
        assertThat(resultado.etapas().get(3).fechaLimite()).isNull();
        assertThat(resultado.etapas().get(3).diasRestantes()).isNull();
        assertThat(resultado.etapas().get(3).vencida()).isFalse();
        assertThat(resultado.historial())
            .extracting(h -> h.getId())
            .containsExactly(30L, 31L);
        assertThat(resultado.requisitos()).isSameAs(requisitos);
        verify(acceso).verificarPuedeVer(1L);
    }

    @Test
    void fechaExactaNoEstaVencida() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(fechas.findByProyectoIdOrderByFechaLimiteAsc(1L)).thenReturn(
            List.of(new FechaLimiteEtapa().etapa(Etapa.DESARROLLO).fechaLimite(AHORA))
        );
        var etapa = service.obtener(1L).etapas().get(2);
        assertThat(etapa.vencida()).isFalse();
        assertThat(etapa.diasRestantes()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoProyecto.class, names = { "APROBADO", "REPROBADO" })
    void finalizadoMarcaTodasLasEtapasCompletadas(EstadoProyecto estado) {
        proyecto.etapaActual(Etapa.FINALIZADO).estado(estado).porcentajeAvance(100);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        assertThat(service.obtener(1L).etapas()).allMatch(e -> e.estado().equals("COMPLETADA"));
    }

    @Test
    void canceladoConservaLaEtapaActual() {
        proyecto.estado(EstadoProyecto.CANCELADO);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        assertThat(service.obtener(1L).etapas().get(2).estado()).isEqualTo("ACTUAL");
    }

    @Test
    void sinPermisoNoConsultaLosDetalles() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        doThrow(new AccessDeniedException("Sin acceso")).when(acceso).verificarPuedeVer(1L);
        assertThatThrownBy(() -> service.obtener(1L)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(entregas, observaciones, evaluaciones, fechas, historial, evaluador);
    }

    @Test
    void inexistenteDevuelve404() {
        assertThatThrownBy(() -> service.obtener(99L)).isInstanceOfSatisfying(ResponseStatusException.class, e ->
            assertThat(e.getStatusCode().value()).isEqualTo(404)
        );
        verifyNoInteractions(acceso, entregas, observaciones, evaluaciones, fechas, historial, evaluador);
    }
}
