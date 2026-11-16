package co.edu.sgpg.service.dto.workflow;

import jakarta.validation.constraints.Size;

public record ComentarioWorkflowDTO(@Size(max = 500) String comentario) {}
