package co.edu.sgpg.repository;

import co.edu.sgpg.domain.FechaLimiteEtapa;
import co.edu.sgpg.domain.enumeration.Etapa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the FechaLimiteEtapa entity.
 */
@Repository
public interface FechaLimiteEtapaRepository extends JpaRepository<FechaLimiteEtapa, Long> {
    default Optional<FechaLimiteEtapa> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<FechaLimiteEtapa> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<FechaLimiteEtapa> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select fechaLimiteEtapa from FechaLimiteEtapa fechaLimiteEtapa left join fetch fechaLimiteEtapa.proyecto",
        countQuery = "select count(fechaLimiteEtapa) from FechaLimiteEtapa fechaLimiteEtapa"
    )
    Page<FechaLimiteEtapa> findAllWithToOneRelationships(Pageable pageable);

    @Query("select fechaLimiteEtapa from FechaLimiteEtapa fechaLimiteEtapa left join fetch fechaLimiteEtapa.proyecto")
    List<FechaLimiteEtapa> findAllWithToOneRelationships();

    @Query(
        "select fechaLimiteEtapa from FechaLimiteEtapa fechaLimiteEtapa left join fetch fechaLimiteEtapa.proyecto where fechaLimiteEtapa.id =:id"
    )
    Optional<FechaLimiteEtapa> findOneWithToOneRelationships(@Param("id") Long id);

    // --- Consultas de reglas de negocio (contrato compartido, ver docs/PLAN.md §5) ---

    Optional<FechaLimiteEtapa> findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(Long proyectoId, Etapa etapa);

    List<FechaLimiteEtapa> findByProyectoIdOrderByFechaLimiteAsc(Long proyectoId);

    @Query(
        "select e from FechaLimiteEtapa e where exists (select p.id from ProyectoGrado p left join p.estudiantes estudiante left join p.tutor tutor left join p.jurados jurado where p.id = e.proyecto.id and (estudiante.login = :login or tutor.login = :login or jurado.login = :login))"
    )
    List<FechaLimiteEtapa> findVisiblesPorLogin(@Param("login") String login);
}
