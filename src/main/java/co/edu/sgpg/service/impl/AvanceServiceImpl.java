package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.service.AvanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación provisional: solo usa el porcentaje base de la etapa.
 * El stream A (workflow) la reemplaza por el cálculo completo con requisitos.
 */
@Service
@Transactional
public class AvanceServiceImpl implements AvanceService {

    private final ProyectoGradoRepository proyectoGradoRepository;

    public AvanceServiceImpl(ProyectoGradoRepository proyectoGradoRepository) {
        this.proyectoGradoRepository = proyectoGradoRepository;
    }

    @Override
    public int recalcular(Long proyectoId) {
        ProyectoGrado proyecto = proyectoGradoRepository.findById(proyectoId).orElseThrow();
        int avance = base(proyecto.getEtapaActual());
        proyecto.setPorcentajeAvance(avance);
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
