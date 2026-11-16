package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import co.edu.sgpg.service.dto.workflow.RequisitosTransicionDTO;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EvaluadorRequisitos {

    private final Map<Etapa, ReglaTransicion> reglas = new EnumMap<>(Etapa.class);

    public EvaluadorRequisitos(List<ReglaTransicion> reglas) {
        for (ReglaTransicion regla : reglas) {
            if (this.reglas.put(regla.origen(), regla) != null) {
                throw new IllegalArgumentException("Hay más de una regla para " + regla.origen());
            }
        }
    }

    public RequisitosTransicionDTO evaluar(ProyectoGrado proyecto) {
        if (proyecto.getEtapaActual() == Etapa.FINALIZADO) {
            return new RequisitosTransicionDTO(proyecto.getId(), Etapa.FINALIZADO, null, false, List.of());
        }
        ReglaTransicion regla = reglas.get(proyecto.getEtapaActual());
        if (regla == null) {
            throw new IllegalStateException("No existe regla para la etapa " + proyecto.getEtapaActual());
        }
        List<RequisitoDTO> requisitos = regla.evaluar(proyecto);
        boolean puedeAvanzar = proyecto.getEstado() == EstadoProyecto.ACTIVO && requisitos.stream().allMatch(RequisitoDTO::cumplido);
        return new RequisitosTransicionDTO(proyecto.getId(), regla.origen(), regla.destino(), puedeAvanzar, requisitos);
    }
}
