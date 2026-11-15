package co.edu.sgpg.repository;

import co.edu.sgpg.domain.HistorialEtapa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the HistorialEtapa entity.
 */
@Repository
public interface HistorialEtapaRepository extends JpaRepository<HistorialEtapa, Long> {
    @Query("select historialEtapa from HistorialEtapa historialEtapa where historialEtapa.usuario.login = ?#{authentication.name}")
    List<HistorialEtapa> findByUsuarioIsCurrentUser();

    default Optional<HistorialEtapa> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<HistorialEtapa> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<HistorialEtapa> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select historialEtapa from HistorialEtapa historialEtapa left join fetch historialEtapa.usuario left join fetch historialEtapa.proyecto",
        countQuery = "select count(historialEtapa) from HistorialEtapa historialEtapa"
    )
    Page<HistorialEtapa> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select historialEtapa from HistorialEtapa historialEtapa left join fetch historialEtapa.usuario left join fetch historialEtapa.proyecto"
    )
    List<HistorialEtapa> findAllWithToOneRelationships();

    @Query(
        "select historialEtapa from HistorialEtapa historialEtapa left join fetch historialEtapa.usuario left join fetch historialEtapa.proyecto where historialEtapa.id =:id"
    )
    Optional<HistorialEtapa> findOneWithToOneRelationships(@Param("id") Long id);

    // --- Consultas de seguimiento (contrato compartido, ver docs/PLAN.md §5) ---

    List<HistorialEtapa> findByProyectoIdOrderByFechaAsc(Long proyectoId);

    @Query(
        "select e from HistorialEtapa e where exists (select p.id from ProyectoGrado p left join p.estudiantes estudiante left join p.tutor tutor left join p.jurados jurado where p.id = e.proyecto.id and (estudiante.login = :login or tutor.login = :login or jurado.login = :login))"
    )
    Page<HistorialEtapa> findVisiblesPorLogin(@Param("login") String login, Pageable pageable);
}
