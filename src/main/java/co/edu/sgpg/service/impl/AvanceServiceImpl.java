package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.service.AvanceService;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import co.edu.sgpg.service.dto.workflow.RequisitosTransicionDTO;
import co.edu.sgpg.service.workflow.EvaluadorRequisitos;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Calcula y persiste el avance académico a partir de los requisitos de la etapa actual.
 */
@Service
@Transactional
public class AvanceServiceImpl implements AvanceService {

    private final ProyectoGradoRepository proyectoGradoRepository;
    private final EvaluadorRequisitos evaluador;

    public AvanceServiceImpl(ProyectoGradoRepository proyectoGradoRepository, EvaluadorRequisitos evaluador) {
        this.proyectoGradoRepository = proyectoGradoRepository;
        this.evaluador = evaluador;
    }

    @Override
    public int recalcular(Long proyectoId) {
        ProyectoGrado proyecto = proyectoGradoRepository
            .findById(proyectoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El proyecto no existe"));
        int avance = base(proyecto.getEtapaActual());
        if (proyecto.getEtapaActual() != Etapa.FINALIZADO) {
            RequisitosTransicionDTO transicion = evaluador.evaluar(proyecto);
            int total = transicion.requisitos().size();
            long cumplidos = transicion.requisitos().stream().filter(RequisitoDTO::cumplido).count();
            if (total > 0) {
                avance += (int) (((base(transicion.etapaSiguiente()) - avance) * cumplidos) / total);
            }
        }
        proyecto.setPorcentajeAvance(avance);
        proyectoGradoRepository.save(proyecto);
        return avance;
    }

    static int base(Etapa etapa) {
        return switch (etapa) {
            case PROPUESTA -> 0;
            case ANTEPROYECTO -> 20;
            case DESARROLLO -> 40;
            case SUSTENTACION -> 75;
            case FINALIZADO -> 100;
        };
    }
}
