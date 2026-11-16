package co.edu.sgpg.service;

import co.edu.sgpg.service.dto.FechaLimiteEtapaDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link co.edu.sgpg.domain.FechaLimiteEtapa}.
 */
public interface FechaLimiteEtapaService {
    /**
     * Save a fechaLimiteEtapa.
     *
     * @param fechaLimiteEtapaDTO the entity to save.
     * @return the persisted entity.
     */
    FechaLimiteEtapaDTO save(FechaLimiteEtapaDTO fechaLimiteEtapaDTO);

    /**
     * Updates a fechaLimiteEtapa.
     *
     * @param fechaLimiteEtapaDTO the entity to update.
     * @return the persisted entity.
     */
    FechaLimiteEtapaDTO update(FechaLimiteEtapaDTO fechaLimiteEtapaDTO);

    /**
     * Partially updates a fechaLimiteEtapa.
     *
     * @param fechaLimiteEtapaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<FechaLimiteEtapaDTO> partialUpdate(FechaLimiteEtapaDTO fechaLimiteEtapaDTO);

    /**
     * Get all the fechaLimiteEtapas.
     *
     * @return the list of entities.
     */
    List<FechaLimiteEtapaDTO> findAll();

    /**
     * Get all the fechaLimiteEtapas with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    List<FechaLimiteEtapaDTO> findAllWithEagerRelationships();

    /**
     * Get the "id" fechaLimiteEtapa.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<FechaLimiteEtapaDTO> findOne(Long id);

    /**
     * Delete the "id" fechaLimiteEtapa.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
