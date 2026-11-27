package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Concepto;
import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.domain.enumeration.TipoObservacion;
import co.edu.sgpg.repository.EntregaRepository;
import co.edu.sgpg.repository.EvaluacionRepository;
import co.edu.sgpg.repository.FechaLimiteEtapaRepository;
import co.edu.sgpg.repository.ObservacionRepository;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Piezas reutilizables de las reglas académicas, acotadas siempre a la etapa actual. */
@Component
public class Requisitos {

    private final EntregaRepository entregas;
    private final ObservacionRepository observaciones;
    private final EvaluacionRepository evaluaciones;
    private final FechaLimiteEtapaRepository fechas;

    public Requisitos(
        EntregaRepository entregas,
        ObservacionRepository observaciones,
        EvaluacionRepository evaluaciones,
        FechaLimiteEtapaRepository fechas
    ) {
        this.entregas = entregas;
        this.observaciones = observaciones;
        this.evaluaciones = evaluaciones;
        this.fechas = fechas;
    }

    public RequisitoDTO tutorAsignado(ProyectoGrado proyecto) {
        return new RequisitoDTO("TUTOR_ASIGNADO", "El proyecto tiene tutor asignado", proyecto.getTutor() != null);
    }

    public RequisitoDTO estudiantesAsignados(ProyectoGrado proyecto) {
        return new RequisitoDTO("ESTUDIANTES_ASIGNADOS", "El proyecto tiene al menos un estudiante", !proyecto.getEstudiantes().isEmpty());
    }

    public RequisitoDTO entregaAprobada(ProyectoGrado proyecto) {
        return new RequisitoDTO(
            "ENTREGA_APROBADA",
            "Existe una entrega aprobada de la etapa actual",
            entregas.existsByProyectoIdAndEtapaAndEstado(proyecto.getId(), proyecto.getEtapaActual(), EstadoEntrega.APROBADA)
        );
    }

    public RequisitoDTO sinCorreccionesPendientes(ProyectoGrado proyecto) {
        return new RequisitoDTO(
            "SIN_CORRECCIONES_PENDIENTES",
            "No hay correcciones requeridas sin resolver en la etapa actual",
            observaciones.countByEntregaProyectoIdAndEntregaEtapaAndTipoAndResueltaFalse(
                proyecto.getId(),
                proyecto.getEtapaActual(),
                TipoObservacion.CORRECCION_REQUERIDA
            ) == 0
        );
    }

    public RequisitoDTO juradosAsignados(ProyectoGrado proyecto, int minimo) {
        return new RequisitoDTO(
            "JURADOS_ASIGNADOS",
            "El proyecto tiene al menos " + minimo + " jurados asignados",
            proyecto.getJurados().size() >= minimo
        );
    }

    public RequisitoDTO evaluacionesCompletas(ProyectoGrado proyecto) {
        Set<Long> juradosQueEvaluaron = evaluacionesActuales(proyecto)
            .stream()
            .map(Evaluacion::getJurado)
            .filter(Objects::nonNull)
            .map(jurado -> jurado.getId())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        // Sin jurados no hay una evaluación académica que permita finalizar el proyecto.
        boolean completas =
            !proyecto.getJurados().isEmpty() &&
            proyecto
                .getJurados()
                .stream()
                .allMatch(jurado -> jurado.getId() != null && juradosQueEvaluaron.contains(jurado.getId()));
        return new RequisitoDTO("EVALUACIONES_COMPLETAS", "Todos los jurados asignados evaluaron la etapa actual", completas);
    }

    public RequisitoDTO evaluacionAprobatoria(ProyectoGrado proyecto) {
        return new RequisitoDTO(
            "EVALUACION_APROBATORIA",
            "El promedio es al menos 3.0 y ninguna evaluación tiene concepto reprobado",
            resultadoAprobatorio(proyecto)
        );
    }

    public boolean resultadoAprobatorio(ProyectoGrado proyecto) {
        List<Evaluacion> actuales = evaluacionesActuales(proyecto);
        if (
            actuales.isEmpty() ||
            actuales.stream().anyMatch(e -> e.getCalificacion() == null || e.getConcepto() == null || e.getConcepto() == Concepto.REPROBADO)
        ) {
            return false;
        }
        BigDecimal suma = actuales.stream().map(Evaluacion::getCalificacion).reduce(BigDecimal.ZERO, BigDecimal::add);
        // Comparación exacta: evita que redondear el promedio convierta una nota inferior a 3 en aprobatoria.
        return suma.compareTo(BigDecimal.valueOf(3).multiply(BigDecimal.valueOf(actuales.size()))) >= 0;
    }

    public RequisitoDTO sustentacionProgramada(ProyectoGrado proyecto) {
        return new RequisitoDTO(
            "SUSTENTACION_PROGRAMADA",
            "Existe una fecha límite para la sustentación",
            fechas.findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(proyecto.getId(), Etapa.SUSTENTACION).isPresent()
        );
    }

    private List<Evaluacion> evaluacionesActuales(ProyectoGrado proyecto) {
        return evaluaciones.findByProyectoIdAndEtapa(proyecto.getId(), proyecto.getEtapaActual());
    }
}
