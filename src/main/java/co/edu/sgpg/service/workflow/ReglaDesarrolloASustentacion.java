package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReglaDesarrolloASustentacion implements ReglaTransicion {

    private final Requisitos requisitos;

    public ReglaDesarrolloASustentacion(Requisitos requisitos) {
        this.requisitos = requisitos;
    }

    @Override
    public Etapa origen() {
        return Etapa.DESARROLLO;
    }

    @Override
    public Etapa destino() {
        return Etapa.SUSTENTACION;
    }

    @Override
    public List<RequisitoDTO> evaluar(ProyectoGrado proyecto) {
        return List.of(
            requisitos.entregaAprobada(proyecto),
            requisitos.sinCorreccionesPendientes(proyecto),
            requisitos.sustentacionProgramada(proyecto)
        );
    }
}
