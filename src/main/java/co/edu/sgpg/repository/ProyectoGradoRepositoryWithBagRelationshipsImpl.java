package co.edu.sgpg.repository;

import co.edu.sgpg.domain.ProyectoGrado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class ProyectoGradoRepositoryWithBagRelationshipsImpl implements ProyectoGradoRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String PROYECTOGRADOS_PARAMETER = "proyectoGrados";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ProyectoGrado> fetchBagRelationships(Optional<ProyectoGrado> proyectoGrado) {
        return proyectoGrado.map(this::fetchEstudianteses).map(this::fetchJuradoses);
    }

    @Override
    public Page<ProyectoGrado> fetchBagRelationships(Page<ProyectoGrado> proyectoGrados) {
        return new PageImpl<>(
            fetchBagRelationships(proyectoGrados.getContent()),
            proyectoGrados.getPageable(),
            proyectoGrados.getTotalElements()
        );
    }

    @Override
    public List<ProyectoGrado> fetchBagRelationships(List<ProyectoGrado> proyectoGrados) {
        return Optional.of(proyectoGrados).map(this::fetchEstudianteses).map(this::fetchJuradoses).orElse(List.of());
    }

    ProyectoGrado fetchEstudianteses(ProyectoGrado result) {
        return entityManager
            .createQuery(
                "select proyectoGrado from ProyectoGrado proyectoGrado left join fetch proyectoGrado.estudianteses where proyectoGrado.id = :id",
                ProyectoGrado.class
            )
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<ProyectoGrado> fetchEstudianteses(List<ProyectoGrado> proyectoGrados) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, proyectoGrados.size()).forEach(index -> order.put(proyectoGrados.get(index).getId(), index));
        List<ProyectoGrado> result = entityManager
            .createQuery(
                "select proyectoGrado from ProyectoGrado proyectoGrado left join fetch proyectoGrado.estudianteses where proyectoGrado in :proyectoGrados",
                ProyectoGrado.class
            )
            .setParameter(PROYECTOGRADOS_PARAMETER, proyectoGrados)
            .getResultList();
        result.sort((o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }

    ProyectoGrado fetchJuradoses(ProyectoGrado result) {
        return entityManager
            .createQuery(
                "select proyectoGrado from ProyectoGrado proyectoGrado left join fetch proyectoGrado.juradoses where proyectoGrado.id = :id",
                ProyectoGrado.class
            )
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<ProyectoGrado> fetchJuradoses(List<ProyectoGrado> proyectoGrados) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, proyectoGrados.size()).forEach(index -> order.put(proyectoGrados.get(index).getId(), index));
        List<ProyectoGrado> result = entityManager
            .createQuery(
                "select proyectoGrado from ProyectoGrado proyectoGrado left join fetch proyectoGrado.juradoses where proyectoGrado in :proyectoGrados",
                ProyectoGrado.class
            )
            .setParameter(PROYECTOGRADOS_PARAMETER, proyectoGrados)
            .getResultList();
        result.sort((o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
