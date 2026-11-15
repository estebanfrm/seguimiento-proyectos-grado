package co.edu.sgpg.service.dto.workflow;

import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import java.time.Instant;

public record EntregaResumenDTO(
    Long id,
    String titulo,
    Integer version,
    EstadoEntrega estado,
    Instant fechaEntrega,
    long observacionesPendientes
) {}
