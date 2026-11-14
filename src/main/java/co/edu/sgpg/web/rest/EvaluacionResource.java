package co.edu.sgpg.web.rest;

import co.edu.sgpg.repository.EvaluacionRepository;
import co.edu.sgpg.service.EvaluacionService;
import co.edu.sgpg.service.dto.EvaluacionDTO;
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
 * REST controller for managing {@link co.edu.sgpg.domain.Evaluacion}.
 */
@RestController
@RequestMapping("/api/evaluacions")
public class EvaluacionResource {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluacionResource.class);

    private static final String ENTITY_NAME = "evaluacion";

    @Value("${jhipster.clientApp.name:sgpg}")
    private String applicationName;

    private final EvaluacionService evaluacionService;

    private final EvaluacionRepository evaluacionRepository;

    public EvaluacionResource(EvaluacionService evaluacionService, EvaluacionRepository evaluacionRepository) {
        this.evaluacionService = evaluacionService;
        this.evaluacionRepository = evaluacionRepository;
    }

    /**
     * {@code POST  /evaluacions} : Create a new evaluacion.
     *
     * @param evaluacionDTO the evaluacionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new evaluacionDTO, or with status {@code 400 (Bad Request)} if the evaluacion already has an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EvaluacionDTO> createEvaluacion(@Valid @RequestBody EvaluacionDTO evaluacionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Evaluacion : {}", evaluacionDTO);
        if (evaluacionDTO.getId() != null) {
            throw new BadRequestAlertException("A new evaluacion cannot already have an ID", ENTITY_NAME, "idexists");
        }
        evaluacionDTO = evaluacionService.save(evaluacionDTO);
        return ResponseEntity.created(new URI("/api/evaluacions/" + evaluacionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, evaluacionDTO.getId().toString()))
            .body(evaluacionDTO);
    }

    /**
     * {@code PUT  /evaluacions/:id} : Updates an existing evaluacion.
     *
     * @param id the id of the evaluacionDTO to save.
     * @param evaluacionDTO the evaluacionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated evaluacionDTO,
     * or with status {@code 400 (Bad Request)} if the evaluacionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the evaluacionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EvaluacionDTO> updateEvaluacion(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EvaluacionDTO evaluacionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Evaluacion : {}, {}", id, evaluacionDTO);
        if (evaluacionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, evaluacionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!evaluacionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        evaluacionDTO = evaluacionService.update(evaluacionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, evaluacionDTO.getId().toString()))
            .body(evaluacionDTO);
    }

    /**
     * {@code PATCH  /evaluacions/:id} : Partial updates given fields of an existing evaluacion, field will ignore if it is null
     *
     * @param id the id of the evaluacionDTO to save.
     * @param evaluacionDTO the evaluacionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated evaluacionDTO,
     * or with status {@code 400 (Bad Request)} if the evaluacionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the evaluacionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the evaluacionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EvaluacionDTO> partialUpdateEvaluacion(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EvaluacionDTO evaluacionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partially update Evaluacion : {}, {}", id, evaluacionDTO);
        if (evaluacionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, evaluacionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!evaluacionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EvaluacionDTO> result = evaluacionService.partialUpdate(evaluacionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, evaluacionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /evaluacions} : get all the Evaluacions.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Evaluacions in body.
     */
    @GetMapping("")
    public List<EvaluacionDTO> getAllEvaluacions(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all Evaluacions");
        if (eagerload) {
            return evaluacionService.findAllWithEagerRelationships();
        } else {
            return evaluacionService.findAll();
        }
    }

    /**
     * {@code GET  /evaluacions/:id} : get the "id" evaluacion.
     *
     * @param id the id of the evaluacionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the evaluacionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EvaluacionDTO> getEvaluacion(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Evaluacion : {}", id);
        Optional<EvaluacionDTO> evaluacionDTO = evaluacionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(evaluacionDTO);
    }

    /**
     * {@code DELETE  /evaluacions/:id} : delete the "id" evaluacion.
     *
     * @param id the id of the evaluacionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvaluacion(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Evaluacion : {}", id);
        evaluacionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
