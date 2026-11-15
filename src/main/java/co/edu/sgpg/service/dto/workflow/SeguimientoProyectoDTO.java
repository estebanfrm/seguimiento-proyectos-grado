package co.edu.sgpg.service.dto.workflow;

import co.edu.sgpg.service.dto.HistorialEtapaDTO;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import java.util.List;

public record SeguimientoProyectoDTO(
    ProyectoGradoDTO proyecto,
    Integer porcentajeAvance,
    List<EtapaSeguimientoDTO> etapas,
    List<HistorialEtapaDTO> historial,
    RequisitosTransicionDTO requisitos
) {}
