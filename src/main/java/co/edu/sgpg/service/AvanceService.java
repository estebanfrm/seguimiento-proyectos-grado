package co.edu.sgpg.service;

/**
 * Calcula y persiste el porcentaje de avance de un proyecto (ver docs/PLAN.md §2).
 * Debe llamarse tras cualquier mutación que afecte los requisitos de transición.
 */
public interface AvanceService {
    /**
     * Recalcula y persiste {@code porcentajeAvance}.
     *
     * @return el nuevo porcentaje (0..100).
     */
    int recalcular(Long proyectoId);
}
