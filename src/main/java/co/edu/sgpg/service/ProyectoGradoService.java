package co.edu.sgpg.service;

import co.edu.sgpg.service.dto.AsignacionesDTO;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link co.edu.sgpg.domain.ProyectoGrado}.
 */
public interface ProyectoGradoService {
    /**
     * Save a proyectoGrado.
     *
     * @param proyectoGradoDTO the entity to save.
     * @return the persisted entity.
     */
    ProyectoGradoDTO save(ProyectoGradoDTO proyectoGradoDTO);

    /**
     * Updates a proyectoGrado.
     *
     * @param proyectoGradoDTO the entity to update.
     * @return the persisted entity.
     */
    ProyectoGradoDTO update(ProyectoGradoDTO proyectoGradoDTO);

    /**
     * Partially updates a proyectoGrado.
     *
     * @param proyectoGradoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ProyectoGradoDTO> partialUpdate(ProyectoGradoDTO proyectoGradoDTO);

    /**
     * Get all the proyectoGrados with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ProyectoGradoDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" proyectoGrado.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ProyectoGradoDTO> findOne(Long id);

    /**
     * Delete the "id" proyectoGrado.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /** Reemplaza todas las asignaciones del proyecto; solo el coordinador puede modificarlas. */
    ProyectoGradoDTO actualizarAsignaciones(Long id, AsignacionesDTO asignaciones);
}
