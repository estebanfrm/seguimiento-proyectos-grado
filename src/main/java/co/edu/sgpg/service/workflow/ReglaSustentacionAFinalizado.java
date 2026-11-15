package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReglaSustentacionAFinalizado implements ReglaTransicion {

    private final Requisitos requisitos;

    public ReglaSustentacionAFinalizado(Requisitos requisitos) {
        this.requisitos = requisitos;
    }

    @Override
    public Etapa origen() {
        return Etapa.SUSTENTACION;
    }

    @Override
    public Etapa destino() {
        return Etapa.FINALIZADO;
    }

    @Override
    public List<RequisitoDTO> evaluar(ProyectoGrado proyecto) {
        return List.of(requisitos.evaluacionesCompletas(proyecto));
    }
}
