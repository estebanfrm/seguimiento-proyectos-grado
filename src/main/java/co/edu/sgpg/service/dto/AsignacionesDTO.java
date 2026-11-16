package co.edu.sgpg.service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Asignaciones completas: un tutor nulo y las listas vacías eliminan las relaciones correspondientes. */
public record AsignacionesDTO(Long tutorId, @NotNull List<Long> juradoIds, @NotNull List<Long> estudianteIds) {}
