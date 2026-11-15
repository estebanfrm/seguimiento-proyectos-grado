package co.edu.sgpg.service.dto.workflow;

import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.EvaluacionDTO;
import java.time.Instant;
import java.util.List;

public record EtapaSeguimientoDTO(
    Etapa etapa,
    String estado,
    Instant fechaLimite,
    boolean vencida,
    Long diasRestantes,
    List<EntregaResumenDTO> entregas,
    List<EvaluacionDTO> evaluaciones
) {}
