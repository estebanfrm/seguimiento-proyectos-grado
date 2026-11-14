package co.edu.sgpg.repository;

import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.domain.enumeration.Etapa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Evaluacion entity.
 */
@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
    @Query("select evaluacion from Evaluacion evaluacion where evaluacion.jurado.login = ?#{authentication.name}")
    List<Evaluacion> findByJuradoIsCurrentUser();

    default Optional<Evaluacion> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Evaluacion> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Evaluacion> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select evaluacion from Evaluacion evaluacion left join fetch evaluacion.jurado left join fetch evaluacion.proyecto",
        countQuery = "select count(evaluacion) from Evaluacion evaluacion"
    )
    Page<Evaluacion> findAllWithToOneRelationships(Pageable pageable);

    @Query("select evaluacion from Evaluacion evaluacion left join fetch evaluacion.jurado left join fetch evaluacion.proyecto")
    List<Evaluacion> findAllWithToOneRelationships();

    @Query(
        "select evaluacion from Evaluacion evaluacion left join fetch evaluacion.jurado left join fetch evaluacion.proyecto where evaluacion.id =:id"
    )
    Optional<Evaluacion> findOneWithToOneRelationships(@Param("id") Long id);

    // --- Consultas de reglas de negocio (contrato compartido, ver docs/PLAN.md §5) ---

    List<Evaluacion> findByProyectoIdAndEtapa(Long proyectoId, Etapa etapa);

    List<Evaluacion> findByProyectoIdOrderByFechaEvaluacionAsc(Long proyectoId);

    boolean existsByProyectoIdAndEtapaAndJuradoLogin(Long proyectoId, Etapa etapa, String login);
}
