package co.edu.sgpg.service;

import co.edu.sgpg.service.dto.EvaluacionDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link co.edu.sgpg.domain.Evaluacion}.
 */
public interface EvaluacionService {
    /**
     * Save a evaluacion.
     *
     * @param evaluacionDTO the entity to save.
     * @return the persisted entity.
     */
    EvaluacionDTO save(EvaluacionDTO evaluacionDTO);

    /**
     * Updates a evaluacion.
     *
     * @param evaluacionDTO the entity to update.
     * @return the persisted entity.
     */
    EvaluacionDTO update(EvaluacionDTO evaluacionDTO);

    /**
     * Partially updates a evaluacion.
     *
     * @param evaluacionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<EvaluacionDTO> partialUpdate(EvaluacionDTO evaluacionDTO);

    /**
     * Get all the evaluacions.
     *
     * @return the list of entities.
     */
    List<EvaluacionDTO> findAll();

    /**
     * Get all the evaluacions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    List<EvaluacionDTO> findAllWithEagerRelationships();

    /**
     * Get the "id" evaluacion.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<EvaluacionDTO> findOne(Long id);

    /**
     * Delete the "id" evaluacion.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
