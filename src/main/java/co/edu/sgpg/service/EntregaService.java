package co.edu.sgpg.service;

import co.edu.sgpg.service.dto.EntregaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link co.edu.sgpg.domain.Entrega}.
 */
public interface EntregaService {
    /**
     * Save a entrega.
     *
     * @param entregaDTO the entity to save.
     * @return the persisted entity.
     */
    EntregaDTO save(EntregaDTO entregaDTO);

    /**
     * Updates a entrega.
     *
     * @param entregaDTO the entity to update.
     * @return the persisted entity.
     */
    EntregaDTO update(EntregaDTO entregaDTO);

    /**
     * Partially updates a entrega.
     *
     * @param entregaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<EntregaDTO> partialUpdate(EntregaDTO entregaDTO);

    /**
     * Get all the entregas with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<EntregaDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" entrega.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<EntregaDTO> findOne(Long id);

    /**
     * Delete the "id" entrega.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    Optional<EntregaDTO> revisar(Long id, co.edu.sgpg.service.dto.RevisionEntregaDTO revision);
}
