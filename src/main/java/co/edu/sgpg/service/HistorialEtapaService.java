package co.edu.sgpg.service;

import co.edu.sgpg.service.dto.HistorialEtapaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link co.edu.sgpg.domain.HistorialEtapa}.
 */
public interface HistorialEtapaService {
    /**
     * Save a historialEtapa.
     *
     * @param historialEtapaDTO the entity to save.
     * @return the persisted entity.
     */
    HistorialEtapaDTO save(HistorialEtapaDTO historialEtapaDTO);

    /**
     * Updates a historialEtapa.
     *
     * @param historialEtapaDTO the entity to update.
     * @return the persisted entity.
     */
    HistorialEtapaDTO update(HistorialEtapaDTO historialEtapaDTO);

    /**
     * Partially updates a historialEtapa.
     *
     * @param historialEtapaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<HistorialEtapaDTO> partialUpdate(HistorialEtapaDTO historialEtapaDTO);

    /**
     * Get all the historialEtapas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<HistorialEtapaDTO> findAll(Pageable pageable);

    /**
     * Get all the historialEtapas with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<HistorialEtapaDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" historialEtapa.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<HistorialEtapaDTO> findOne(Long id);

    /**
     * Delete the "id" historialEtapa.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
