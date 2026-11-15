package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReglaAnteproyectoADesarrollo implements ReglaTransicion {

    private final Requisitos requisitos;

    public ReglaAnteproyectoADesarrollo(Requisitos requisitos) {
        this.requisitos = requisitos;
    }

    @Override
    public Etapa origen() {
        return Etapa.ANTEPROYECTO;
    }

    @Override
    public Etapa destino() {
        return Etapa.DESARROLLO;
    }

    @Override
    public List<RequisitoDTO> evaluar(ProyectoGrado proyecto) {
        return List.of(
            requisitos.entregaAprobada(proyecto),
            requisitos.sinCorreccionesPendientes(proyecto),
            requisitos.juradosAsignados(proyecto, 2),
            requisitos.evaluacionesCompletas(proyecto),
            requisitos.evaluacionAprobatoria(proyecto)
        );
    }
}
