package co.edu.sgpg.repository;

import co.edu.sgpg.domain.Observacion;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.domain.enumeration.TipoObservacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Observacion entity.
 */
@Repository
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {
    @Query("select observacion from Observacion observacion where observacion.autor.login = ?#{authentication.name}")
    List<Observacion> findByAutorIsCurrentUser();

    default Optional<Observacion> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Observacion> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Observacion> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select observacion from Observacion observacion left join fetch observacion.autor left join fetch observacion.entrega",
        countQuery = "select count(observacion) from Observacion observacion"
    )
    Page<Observacion> findAllWithToOneRelationships(Pageable pageable);

    @Query("select observacion from Observacion observacion left join fetch observacion.autor left join fetch observacion.entrega")
    List<Observacion> findAllWithToOneRelationships();

    @Query(
        "select observacion from Observacion observacion left join fetch observacion.autor left join fetch observacion.entrega where observacion.id =:id"
    )
    Optional<Observacion> findOneWithToOneRelationships(@Param("id") Long id);

    // --- Consultas de reglas de negocio (contrato compartido, ver docs/PLAN.md §5) ---

    long countByEntregaProyectoIdAndEntregaEtapaAndTipoAndResueltaFalse(Long proyectoId, Etapa etapa, TipoObservacion tipo);

    long countByEntregaIdAndTipoAndResueltaFalse(Long entregaId, TipoObservacion tipo);

    long countByEntregaIdAndResueltaFalse(Long entregaId);

    List<Observacion> findByEntregaIdOrderByFechaCreacionAsc(Long entregaId);
}
