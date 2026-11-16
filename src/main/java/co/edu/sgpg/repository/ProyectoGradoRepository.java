package co.edu.sgpg.repository;

import co.edu.sgpg.domain.ProyectoGrado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ProyectoGrado entity.
 *
 * When extending this class, extend ProyectoGradoRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface ProyectoGradoRepository
    extends ProyectoGradoRepositoryWithBagRelationships, JpaRepository<ProyectoGrado, Long>, JpaSpecificationExecutor<ProyectoGrado>
{
    @Query("select proyectoGrado from ProyectoGrado proyectoGrado where proyectoGrado.tutor.login = ?#{authentication.name}")
    List<ProyectoGrado> findByTutorIsCurrentUser();

    default Optional<ProyectoGrado> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<ProyectoGrado> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<ProyectoGrado> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(
        value = "select proyectoGrado from ProyectoGrado proyectoGrado left join fetch proyectoGrado.tutor",
        countQuery = "select count(proyectoGrado) from ProyectoGrado proyectoGrado"
    )
    Page<ProyectoGrado> findAllWithToOneRelationships(Pageable pageable);

    @Query("select proyectoGrado from ProyectoGrado proyectoGrado left join fetch proyectoGrado.tutor")
    List<ProyectoGrado> findAllWithToOneRelationships();

    @Query("select proyectoGrado from ProyectoGrado proyectoGrado left join fetch proyectoGrado.tutor where proyectoGrado.id =:id")
    Optional<ProyectoGrado> findOneWithToOneRelationships(@Param("id") Long id);

    // --- Consultas de acceso (contrato compartido, ver docs/PLAN.md §5) ---

    boolean existsByIdAndEstudiantesLogin(Long id, String login);

    boolean existsByIdAndTutorLogin(Long id, String login);

    boolean existsByIdAndJuradosLogin(Long id, String login);

    List<ProyectoGrado> findByEstado(co.edu.sgpg.domain.enumeration.EstadoProyecto estado);
}
