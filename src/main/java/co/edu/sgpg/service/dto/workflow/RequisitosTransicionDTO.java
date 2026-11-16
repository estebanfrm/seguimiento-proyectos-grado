package co.edu.sgpg.service.dto.workflow;

import co.edu.sgpg.domain.enumeration.Etapa;
import java.util.List;

public record RequisitosTransicionDTO(
    Long proyectoId,
    Etapa etapaActual,
    Etapa etapaSiguiente,
    boolean puedeAvanzar,
    List<RequisitoDTO> requisitos
) {
    public RequisitosTransicionDTO {
        requisitos = List.copyOf(requisitos);
    }
}
