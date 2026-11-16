package co.edu.sgpg.service.notificacion;

import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.repository.FechaLimiteEtapaRepository;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Una notificación por proyecto en las ventanas de tres días y un día naturales según Clock. */
@Component
public class RecordatorioFechasLimiteJob {

    private static final Logger LOG = LoggerFactory.getLogger(RecordatorioFechasLimiteJob.class);

    private final ProyectoGradoRepository proyectos;
    private final FechaLimiteEtapaRepository fechas;
    private final NotificacionService notificaciones;
    private final Clock clock;
    private final boolean habilitadas;

    public RecordatorioFechasLimiteJob(
        ProyectoGradoRepository proyectos,
        FechaLimiteEtapaRepository fechas,
        NotificacionService notificaciones,
        Clock clock,
        @Value("${sgpg.notificaciones.habilitadas:true}") boolean habilitadas
    ) {
        this.proyectos = proyectos;
        this.fechas = fechas;
        this.notificaciones = notificaciones;
        this.clock = clock;
        this.habilitadas = habilitadas;
    }

    @Scheduled(cron = "${sgpg.notificaciones.recordatorio-cron:0 0 7 * * *}")
    public void recordar() {
        if (!habilitadas) return;
        LocalDate hoy = LocalDate.now(clock);
        proyectos
            .findByEstado(EstadoProyecto.ACTIVO)
            .stream()
            .filter(p -> p.getEstado() == EstadoProyecto.ACTIVO)
            .forEach(proyecto -> {
                try {
                    fechas
                        .findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(proyecto.getId(), proyecto.getEtapaActual())
                        .ifPresent(fecha -> {
                            long dias = ChronoUnit.DAYS.between(hoy, fecha.getFechaLimite().atZone(clock.getZone()).toLocalDate());
                            if (dias == 3 || dias == 1) {
                                notificaciones.recordarFechaLimite(
                                    proyecto.getId(),
                                    proyecto.getEtapaActual(),
                                    fecha.getFechaLimite(),
                                    dias
                                );
                            }
                        });
                } catch (RuntimeException e) {
                    LOG.warn("No se pudo preparar el recordatorio del proyecto {}", proyecto.getId(), e);
                }
            });
    }
}
