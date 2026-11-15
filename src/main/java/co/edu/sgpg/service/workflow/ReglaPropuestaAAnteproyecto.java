package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReglaPropuestaAAnteproyecto implements ReglaTransicion {

    private final Requisitos requisitos;

    public ReglaPropuestaAAnteproyecto(Requisitos requisitos) {
        this.requisitos = requisitos;
    }

    @Override
    public Etapa origen() {
        return Etapa.PROPUESTA;
    }

    @Override
    public Etapa destino() {
        return Etapa.ANTEPROYECTO;
    }

    @Override
    public List<RequisitoDTO> evaluar(ProyectoGrado proyecto) {
        return List.of(
            requisitos.tutorAsignado(proyecto),
            requisitos.estudiantesAsignados(proyecto),
            requisitos.entregaAprobada(proyecto),
            requisitos.sinCorreccionesPendientes(proyecto)
        );
    }
}
