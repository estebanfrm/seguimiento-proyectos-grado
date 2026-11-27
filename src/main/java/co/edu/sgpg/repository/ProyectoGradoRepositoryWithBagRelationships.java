package co.edu.sgpg.repository;

import co.edu.sgpg.domain.ProyectoGrado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProyectoGradoRepositoryWithBagRelationships {
    Optional<ProyectoGrado> fetchBagRelationships(Optional<ProyectoGrado> proyectoGrado);

    List<ProyectoGrado> fetchBagRelationships(List<ProyectoGrado> proyectoGrados);

    Page<ProyectoGrado> fetchBagRelationships(Page<ProyectoGrado> proyectoGrados);
}
