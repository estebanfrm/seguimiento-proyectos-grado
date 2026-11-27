package co.edu.sgpg.repository;

import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.Etapa;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Entrega entity.
 */
@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long>, JpaSpecificationExecutor<Entrega> {
    @Query("select entrega from Entrega entrega where entrega.autor.login = ?#{authentication.name}")
    List<Entrega> findByAutorIsCurrentUser();

    default Optional<Entrega> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Entrega> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Entrega> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select entrega from Entrega entrega left join fetch entrega.autor left join fetch entrega.proyecto",
        countQuery = "select count(entrega) from Entrega entrega"
    )
    Page<Entrega> findAllWithToOneRelationships(Pageable pageable);

    @Query("select entrega from Entrega entrega left join fetch entrega.autor left join fetch entrega.proyecto")
    List<Entrega> findAllWithToOneRelationships();

    @Query("select entrega from Entrega entrega left join fetch entrega.autor left join fetch entrega.proyecto where entrega.id =:id")
    Optional<Entrega> findOneWithToOneRelationships(@Param("id") Long id);

    // --- Consultas de reglas de negocio (contrato compartido, ver docs/PLAN.md §5) ---

    boolean existsByProyectoIdAndEtapaAndEstado(Long proyectoId, Etapa etapa, EstadoEntrega estado);

    boolean existsByProyectoIdAndEtapaAndEstadoIn(Long proyectoId, Etapa etapa, Collection<EstadoEntrega> estados);

    List<Entrega> findByProyectoIdOrderByEtapaAscVersionAsc(Long proyectoId);

    @Query("select coalesce(max(e.version), 0) from Entrega e where e.proyecto.id = :proyectoId and e.etapa = :etapa")
    int findMaxVersion(@Param("proyectoId") Long proyectoId, @Param("etapa") Etapa etapa);
}
