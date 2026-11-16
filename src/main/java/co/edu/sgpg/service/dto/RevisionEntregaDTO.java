package co.edu.sgpg.service.dto;

import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import jakarta.validation.constraints.NotNull;

/** Estado solicitado por el tutor en la revisión de una entrega. */
public record RevisionEntregaDTO(@NotNull EstadoEntrega estado) {}
