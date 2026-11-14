package co.edu.sgpg.web.rest;

import co.edu.sgpg.repository.ObservacionRepository;
import co.edu.sgpg.service.ObservacionService;
import co.edu.sgpg.service.dto.ObservacionDTO;
import co.edu.sgpg.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link co.edu.sgpg.domain.Observacion}.
 */
@RestController
@RequestMapping("/api/observacions")
public class ObservacionResource {

    private static final Logger LOG = LoggerFactory.getLogger(ObservacionResource.class);

    private static final String ENTITY_NAME = "observacion";

    @Value("${jhipster.clientApp.name:sgpg}")
    private String applicationName;

    private final ObservacionService observacionService;

    private final ObservacionRepository observacionRepository;

    public ObservacionResource(ObservacionService observacionService, ObservacionRepository observacionRepository) {
        this.observacionService = observacionService;
        this.observacionRepository = observacionRepository;
    }

    /**
     * {@code POST  /observacions} : Create a new observacion.
     *
     * @param observacionDTO the observacionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new observacionDTO, or with status {@code 400 (Bad Request)} if the observacion already has an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ObservacionDTO> createObservacion(@Valid @RequestBody ObservacionDTO observacionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Observacion : {}", observacionDTO);
        if (observacionDTO.getId() != null) {
            throw new BadRequestAlertException("A new observacion cannot already have an ID", ENTITY_NAME, "idexists");
        }
        observacionDTO = observacionService.save(observacionDTO);
        return ResponseEntity.created(new URI("/api/observacions/" + observacionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, observacionDTO.getId().toString()))
            .body(observacionDTO);
    }

    /**
     * {@code PUT  /observacions/:id} : Updates an existing observacion.
     *
     * @param id the id of the observacionDTO to save.
     * @param observacionDTO the observacionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated observacionDTO,
     * or with status {@code 400 (Bad Request)} if the observacionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the observacionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ObservacionDTO> updateObservacion(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ObservacionDTO observacionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Observacion : {}, {}", id, observacionDTO);
        if (observacionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, observacionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!observacionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        observacionDTO = observacionService.update(observacionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, observacionDTO.getId().toString()))
            .body(observacionDTO);
    }

    /**
     * {@code PATCH  /observacions/:id} : Partial updates given fields of an existing observacion, field will ignore if it is null
     *
     * @param id the id of the observacionDTO to save.
     * @param observacionDTO the observacionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated observacionDTO,
     * or with status {@code 400 (Bad Request)} if the observacionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the observacionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the observacionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ObservacionDTO> partialUpdateObservacion(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ObservacionDTO observacionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partially update Observacion : {}, {}", id, observacionDTO);
        if (observacionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, observacionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!observacionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ObservacionDTO> result = observacionService.partialUpdate(observacionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, observacionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /observacions} : get all the Observacions.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Observacions in body.
     */
    @GetMapping("")
    public List<ObservacionDTO> getAllObservacions(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all Observacions");
        if (eagerload) {
            return observacionService.findAllWithEagerRelationships();
        } else {
            return observacionService.findAll();
        }
    }

    /**
     * {@code GET  /observacions/:id} : get the "id" observacion.
     *
     * @param id the id of the observacionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the observacionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ObservacionDTO> getObservacion(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Observacion : {}", id);
        Optional<ObservacionDTO> observacionDTO = observacionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(observacionDTO);
    }

    /**
     * {@code DELETE  /observacions/:id} : delete the "id" observacion.
     *
     * @param id the id of the observacionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObservacion(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Observacion : {}", id);
        observacionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
