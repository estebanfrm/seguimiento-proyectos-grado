package co.edu.sgpg.service.notificacion;

import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.FechaLimiteEtapa;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.FechaLimiteEtapaRepository;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordatorioFechasLimiteJobTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-13T23:30:00Z"), ZoneOffset.UTC);

    @Mock
    private ProyectoGradoRepository proyectos;

    @Mock
    private FechaLimiteEtapaRepository fechas;

    @Mock
    private NotificacionService notificaciones;

    private RecordatorioFechasLimiteJob job;
    private ProyectoGrado proyecto;

    @BeforeEach
    void preparar() {
        job = new RecordatorioFechasLimiteJob(proyectos, fechas, notificaciones, RELOJ, true);
        proyecto = new ProyectoGrado().id(10L).estado(EstadoProyecto.ACTIVO).etapaActual(Etapa.DESARROLLO);
    }

    @ParameterizedTest
    @ValueSource(ints = { 3, 1 })
    void avisaATresDiasYUnDiaNaturales(int dias) {
        // Se cuentan fechas del calendario, incluso cuando faltan menos de 24 horas.
        Instant limite = Instant.parse("2026-11-13T00:00:00Z").plus(dias, ChronoUnit.DAYS);
        prepararFecha(limite);
        job.recordar();
        verify(notificaciones).recordarFechaLimite(10L, Etapa.DESARROLLO, limite, dias);
        verify(fechas).findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(10L, Etapa.DESARROLLO);
        verifyNoMoreInteractions(notificaciones, fechas);
    }

    @ParameterizedTest
    @ValueSource(ints = { 4, 2, 0, -1 })
    void noAvisaFueraDeLasVentanas(int dias) {
        prepararFecha(Instant.now(RELOJ).plus(dias, ChronoUnit.DAYS));
        job.recordar();
        verifyNoInteractions(notificaciones);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoProyecto.class, names = { "CANCELADO", "APROBADO", "REPROBADO" })
    void noAvisaProyectosNoActivos(EstadoProyecto estado) {
        proyecto.setEstado(estado);
        when(proyectos.findByEstado(EstadoProyecto.ACTIVO)).thenReturn(List.of(proyecto));
        job.recordar();
        verifyNoInteractions(fechas, notificaciones);
    }

    @Test
    void noAvisaSiNoHayFechaParaLaEtapaActual() {
        when(proyectos.findByEstado(EstadoProyecto.ACTIVO)).thenReturn(List.of(proyecto));
        when(fechas.findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(10L, Etapa.DESARROLLO)).thenReturn(Optional.empty());
        job.recordar();
        verifyNoInteractions(notificaciones);
    }

    @Test
    void deshabilitadoNoConsultaLaBaseDeDatos() {
        new RecordatorioFechasLimiteJob(proyectos, fechas, notificaciones, RELOJ, false).recordar();
        verifyNoInteractions(proyectos, fechas, notificaciones);
    }

    private void prepararFecha(Instant limite) {
        when(proyectos.findByEstado(EstadoProyecto.ACTIVO)).thenReturn(List.of(proyecto));
        when(fechas.findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(10L, Etapa.DESARROLLO)).thenReturn(
            Optional.of(new FechaLimiteEtapa().proyecto(proyecto).etapa(Etapa.DESARROLLO).fechaLimite(limite))
        );
    }
}
