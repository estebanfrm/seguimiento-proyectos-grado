package co.edu.sgpg.web.rest;

import co.edu.sgpg.repository.HistorialEtapaRepository;
import co.edu.sgpg.service.HistorialEtapaService;
import co.edu.sgpg.service.dto.HistorialEtapaDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link co.edu.sgpg.domain.HistorialEtapa}.
 */
@RestController
@RequestMapping("/api/historial-etapas")
public class HistorialEtapaResource {

    private static final Logger LOG = LoggerFactory.getLogger(HistorialEtapaResource.class);

    private static final String ENTITY_NAME = "historialEtapa";

    @Value("${jhipster.clientApp.name:sgpg}")
    private String applicationName;

    private final HistorialEtapaService historialEtapaService;

    private final HistorialEtapaRepository historialEtapaRepository;

    public HistorialEtapaResource(HistorialEtapaService historialEtapaService, HistorialEtapaRepository historialEtapaRepository) {
        this.historialEtapaService = historialEtapaService;
        this.historialEtapaRepository = historialEtapaRepository;
    }

    /**
     * {@code POST  /historial-etapas} : Create a new historialEtapa.
     *
     * @param historialEtapaDTO the historialEtapaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new historialEtapaDTO, or with status {@code 400 (Bad Request)} if the historialEtapa already has an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<HistorialEtapaDTO> createHistorialEtapa(@Valid @RequestBody HistorialEtapaDTO historialEtapaDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save HistorialEtapa : {}", historialEtapaDTO);
        if (historialEtapaDTO.getId() != null) {
            throw new BadRequestAlertException("A new historialEtapa cannot already have an ID", ENTITY_NAME, "idexists");
        }
        historialEtapaDTO = historialEtapaService.save(historialEtapaDTO);
        return ResponseEntity.created(new URI("/api/historial-etapas/" + historialEtapaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, historialEtapaDTO.getId().toString()))
            .body(historialEtapaDTO);
    }

    /**
     * {@code PUT  /historial-etapas/:id} : Updates an existing historialEtapa.
     *
     * @param id the id of the historialEtapaDTO to save.
     * @param historialEtapaDTO the historialEtapaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated historialEtapaDTO,
     * or with status {@code 400 (Bad Request)} if the historialEtapaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the historialEtapaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HistorialEtapaDTO> updateHistorialEtapa(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody HistorialEtapaDTO historialEtapaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update HistorialEtapa : {}, {}", id, historialEtapaDTO);
        if (historialEtapaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, historialEtapaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!historialEtapaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        historialEtapaDTO = historialEtapaService.update(historialEtapaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, historialEtapaDTO.getId().toString()))
            .body(historialEtapaDTO);
    }

    /**
     * {@code PATCH  /historial-etapas/:id} : Partial updates given fields of an existing historialEtapa, field will ignore if it is null
     *
     * @param id the id of the historialEtapaDTO to save.
     * @param historialEtapaDTO the historialEtapaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated historialEtapaDTO,
     * or with status {@code 400 (Bad Request)} if the historialEtapaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the historialEtapaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the historialEtapaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<HistorialEtapaDTO> partialUpdateHistorialEtapa(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody HistorialEtapaDTO historialEtapaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partially update HistorialEtapa : {}, {}", id, historialEtapaDTO);
        if (historialEtapaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, historialEtapaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!historialEtapaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<HistorialEtapaDTO> result = historialEtapaService.partialUpdate(historialEtapaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, historialEtapaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /historial-etapas} : get all the Historial Etapas.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Historial Etapas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<HistorialEtapaDTO>> getAllHistorialEtapas(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of HistorialEtapas");
        Page<HistorialEtapaDTO> page;
        if (eagerload) {
            page = historialEtapaService.findAllWithEagerRelationships(pageable);
        } else {
            page = historialEtapaService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /historial-etapas/:id} : get the "id" historialEtapa.
     *
     * @param id the id of the historialEtapaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the historialEtapaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HistorialEtapaDTO> getHistorialEtapa(@PathVariable("id") Long id) {
        LOG.debug("REST request to get HistorialEtapa : {}", id);
        Optional<HistorialEtapaDTO> historialEtapaDTO = historialEtapaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(historialEtapaDTO);
    }

    /**
     * {@code DELETE  /historial-etapas/:id} : delete the "id" historialEtapa.
     *
     * @param id the id of the historialEtapaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistorialEtapa(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete HistorialEtapa : {}", id);
        historialEtapaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
