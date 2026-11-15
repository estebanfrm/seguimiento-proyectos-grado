package co.edu.sgpg.web.rest;

import co.edu.sgpg.repository.FechaLimiteEtapaRepository;
import co.edu.sgpg.security.AuthoritiesConstants;
import co.edu.sgpg.service.FechaLimiteEtapaService;
import co.edu.sgpg.service.dto.FechaLimiteEtapaDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link co.edu.sgpg.domain.FechaLimiteEtapa}.
 */
@RestController
@RequestMapping("/api/fecha-limite-etapas")
public class FechaLimiteEtapaResource {

    private static final Logger LOG = LoggerFactory.getLogger(FechaLimiteEtapaResource.class);

    private static final String ENTITY_NAME = "fechaLimiteEtapa";

    @Value("${jhipster.clientApp.name:sgpg}")
    private String applicationName;

    private final FechaLimiteEtapaService fechaLimiteEtapaService;

    private final FechaLimiteEtapaRepository fechaLimiteEtapaRepository;

    public FechaLimiteEtapaResource(
        FechaLimiteEtapaService fechaLimiteEtapaService,
        FechaLimiteEtapaRepository fechaLimiteEtapaRepository
    ) {
        this.fechaLimiteEtapaService = fechaLimiteEtapaService;
        this.fechaLimiteEtapaRepository = fechaLimiteEtapaRepository;
    }

    /**
     * {@code POST  /fecha-limite-etapas} : Create a new fechaLimiteEtapa.
     *
     * @param fechaLimiteEtapaDTO the fechaLimiteEtapaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new fechaLimiteEtapaDTO, or with status {@code 400 (Bad Request)} if the fechaLimiteEtapa already has an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    @PostMapping("")
    public ResponseEntity<FechaLimiteEtapaDTO> createFechaLimiteEtapa(@Valid @RequestBody FechaLimiteEtapaDTO fechaLimiteEtapaDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save FechaLimiteEtapa : {}", fechaLimiteEtapaDTO);
        if (fechaLimiteEtapaDTO.getId() != null) {
            throw new BadRequestAlertException("A new fechaLimiteEtapa cannot already have an ID", ENTITY_NAME, "idexists");
        }
        fechaLimiteEtapaDTO = fechaLimiteEtapaService.save(fechaLimiteEtapaDTO);
        return ResponseEntity.created(new URI("/api/fecha-limite-etapas/" + fechaLimiteEtapaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, fechaLimiteEtapaDTO.getId().toString()))
            .body(fechaLimiteEtapaDTO);
    }

    /**
     * {@code PUT  /fecha-limite-etapas/:id} : Updates an existing fechaLimiteEtapa.
     *
     * @param id the id of the fechaLimiteEtapaDTO to save.
     * @param fechaLimiteEtapaDTO the fechaLimiteEtapaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fechaLimiteEtapaDTO,
     * or with status {@code 400 (Bad Request)} if the fechaLimiteEtapaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the fechaLimiteEtapaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    @PutMapping("/{id}")
    public ResponseEntity<FechaLimiteEtapaDTO> updateFechaLimiteEtapa(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FechaLimiteEtapaDTO fechaLimiteEtapaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update FechaLimiteEtapa : {}, {}", id, fechaLimiteEtapaDTO);
        if (fechaLimiteEtapaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fechaLimiteEtapaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fechaLimiteEtapaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        fechaLimiteEtapaDTO = fechaLimiteEtapaService.update(fechaLimiteEtapaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fechaLimiteEtapaDTO.getId().toString()))
            .body(fechaLimiteEtapaDTO);
    }

    /**
     * {@code PATCH  /fecha-limite-etapas/:id} : Partial updates given fields of an existing fechaLimiteEtapa, field will ignore if it is null
     *
     * @param id the id of the fechaLimiteEtapaDTO to save.
     * @param fechaLimiteEtapaDTO the fechaLimiteEtapaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fechaLimiteEtapaDTO,
     * or with status {@code 400 (Bad Request)} if the fechaLimiteEtapaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the fechaLimiteEtapaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the fechaLimiteEtapaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FechaLimiteEtapaDTO> partialUpdateFechaLimiteEtapa(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FechaLimiteEtapaDTO fechaLimiteEtapaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partially update FechaLimiteEtapa : {}, {}", id, fechaLimiteEtapaDTO);
        if (fechaLimiteEtapaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fechaLimiteEtapaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fechaLimiteEtapaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FechaLimiteEtapaDTO> result = fechaLimiteEtapaService.partialUpdate(fechaLimiteEtapaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fechaLimiteEtapaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /fecha-limite-etapas} : get all the Fecha Limite Etapas.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Fecha Limite Etapas in body.
     */
    @GetMapping("")
    public List<FechaLimiteEtapaDTO> getAllFechaLimiteEtapas(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all FechaLimiteEtapas");
        if (eagerload) {
            return fechaLimiteEtapaService.findAllWithEagerRelationships();
        } else {
            return fechaLimiteEtapaService.findAll();
        }
    }

    /**
     * {@code GET  /fecha-limite-etapas/:id} : get the "id" fechaLimiteEtapa.
     *
     * @param id the id of the fechaLimiteEtapaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fechaLimiteEtapaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FechaLimiteEtapaDTO> getFechaLimiteEtapa(@PathVariable("id") Long id) {
        LOG.debug("REST request to get FechaLimiteEtapa : {}", id);
        Optional<FechaLimiteEtapaDTO> fechaLimiteEtapaDTO = fechaLimiteEtapaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(fechaLimiteEtapaDTO);
    }

    /**
     * {@code DELETE  /fecha-limite-etapas/:id} : delete the "id" fechaLimiteEtapa.
     *
     * @param id the id of the fechaLimiteEtapaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFechaLimiteEtapa(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete FechaLimiteEtapa : {}", id);
        fechaLimiteEtapaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
