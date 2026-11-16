package co.edu.sgpg.service;

import co.edu.sgpg.service.dto.ObservacionDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link co.edu.sgpg.domain.Observacion}.
 */
public interface ObservacionService {
    /**
     * Save a observacion.
     *
     * @param observacionDTO the entity to save.
     * @return the persisted entity.
     */
    ObservacionDTO save(ObservacionDTO observacionDTO);

    /**
     * Updates a observacion.
     *
     * @param observacionDTO the entity to update.
     * @return the persisted entity.
     */
    ObservacionDTO update(ObservacionDTO observacionDTO);

    /**
     * Partially updates a observacion.
     *
     * @param observacionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ObservacionDTO> partialUpdate(ObservacionDTO observacionDTO);

    /**
     * Get all the observacions.
     *
     * @return the list of entities.
     */
    List<ObservacionDTO> findAll();

    /**
     * Get all the observacions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    List<ObservacionDTO> findAllWithEagerRelationships();

    /**
     * Get the "id" observacion.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ObservacionDTO> findOne(Long id);

    /**
     * Delete the "id" observacion.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    Optional<ObservacionDTO> resolver(Long id);

    /** Observaciones de una entrega visible, ordenadas por fecha; vacío si la entrega no existe. */
    Optional<List<ObservacionDTO>> findByEntrega(Long entregaId);
}
