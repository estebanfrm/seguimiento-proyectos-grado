package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.util.List;

public interface ReglaTransicion {
    Etapa origen();
    Etapa destino();
    List<RequisitoDTO> evaluar(ProyectoGrado proyecto);
}
