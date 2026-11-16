package co.edu.sgpg.web.rest;

import co.edu.sgpg.repository.EntregaRepository;
import co.edu.sgpg.security.AuthoritiesConstants;
import co.edu.sgpg.service.EntregaQueryService;
import co.edu.sgpg.service.EntregaService;
import co.edu.sgpg.service.ObservacionService;
import co.edu.sgpg.service.criteria.EntregaCriteria;
import co.edu.sgpg.service.dto.EntregaDTO;
import co.edu.sgpg.service.dto.ObservacionDTO;
import co.edu.sgpg.service.dto.RevisionEntregaDTO;
import co.edu.sgpg.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link co.edu.sgpg.domain.Entrega}.
 */
@RestController
@RequestMapping("/api/entregas")
public class EntregaResource {

    private static final Logger LOG = LoggerFactory.getLogger(EntregaResource.class);

    private static final String ENTITY_NAME = "entrega";

    @Value("${jhipster.clientApp.name:sgpg}")
    private String applicationName;

    private final EntregaService entregaService;

    private final EntregaRepository entregaRepository;

    private final EntregaQueryService entregaQueryService;

    private final ObservacionService observacionService;

    public EntregaResource(
        EntregaService entregaService,
        EntregaRepository entregaRepository,
        EntregaQueryService entregaQueryService,
        ObservacionService observacionService
    ) {
        this.entregaService = entregaService;
        this.entregaRepository = entregaRepository;
        this.entregaQueryService = entregaQueryService;
        this.observacionService = observacionService;
    }

    /**
     * {@code POST  /entregas} : Create a new entrega.
     *
     * @param entregaDTO the entregaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new entregaDTO, or with status {@code 400 (Bad Request)} if the entrega already has an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ESTUDIANTE + "')")
    @PostMapping("")
    public ResponseEntity<EntregaDTO> createEntrega(@Valid @RequestBody EntregaDTO entregaDTO) throws URISyntaxException {
        LOG.debug("REST request to save Entrega : {}", entregaDTO);
        if (entregaDTO.getId() != null) {
            throw new BadRequestAlertException("A new entrega cannot already have an ID", ENTITY_NAME, "idexists");
        }
        entregaDTO = entregaService.save(entregaDTO);
        return ResponseEntity.created(new URI("/api/entregas/" + entregaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, entregaDTO.getId().toString()))
            .body(entregaDTO);
    }

    /**
     * {@code PUT  /entregas/:id} : Updates an existing entrega.
     *
     * @param id the id of the entregaDTO to save.
     * @param entregaDTO the entregaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated entregaDTO,
     * or with status {@code 400 (Bad Request)} if the entregaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the entregaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "') or hasAuthority('" + AuthoritiesConstants.ESTUDIANTE + "')")
    @PutMapping("/{id}")
    public ResponseEntity<EntregaDTO> updateEntrega(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EntregaDTO entregaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Entrega : {}, {}", id, entregaDTO);
        if (entregaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, entregaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!entregaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        entregaDTO = entregaService.update(entregaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, entregaDTO.getId().toString()))
            .body(entregaDTO);
    }

    /**
     * {@code PATCH  /entregas/:id} : Partial updates given fields of an existing entrega, field will ignore if it is null
     *
     * @param id the id of the entregaDTO to save.
     * @param entregaDTO the entregaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated entregaDTO,
     * or with status {@code 400 (Bad Request)} if the entregaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the entregaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the entregaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "') or hasAuthority('" + AuthoritiesConstants.ESTUDIANTE + "')")
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EntregaDTO> partialUpdateEntrega(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EntregaDTO entregaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partially update Entrega : {}, {}", id, entregaDTO);
        if (entregaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, entregaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!entregaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EntregaDTO> result = entregaService.partialUpdate(entregaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, entregaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /entregas} : get all the Entregas.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Entregas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EntregaDTO>> getAllEntregas(
        EntregaCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Entregas by criteria: {}", criteria);

        Page<EntregaDTO> page = entregaQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /entregas/count} : count all the entregas.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countEntregas(EntregaCriteria criteria) {
        LOG.debug("REST request to count Entregas by criteria: {}", criteria);
        return ResponseEntity.ok().body(entregaQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /entregas/:id} : get the "id" entrega.
     *
     * @param id the id of the entregaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the entregaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntregaDTO> getEntrega(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Entrega : {}", id);
        Optional<EntregaDTO> entregaDTO = entregaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(entregaDTO);
    }

    /**
     * {@code DELETE  /entregas/:id} : delete the "id" entrega.
     *
     * @param id the id of the entregaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntrega(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Entrega : {}", id);
        entregaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/{id}/observacions")
    public ResponseEntity<List<ObservacionDTO>> getObservacions(@PathVariable Long id) {
        return ResponseUtil.wrapOrNotFound(observacionService.findByEntrega(id));
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable Long id) {
        return entregaService
            .obtenerArchivo(id)
            .map(archivo ->
                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + URLEncoder.encode(archivo.nombre(), StandardCharsets.UTF_8).replace("+", "%20")
                    )
                    .body(archivo.contenido())
            )
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/revision")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "') or hasAuthority('" + AuthoritiesConstants.TUTOR + "')")
    public ResponseEntity<EntregaDTO> revisar(@PathVariable Long id, @Valid @RequestBody RevisionEntregaDTO revision) {
        return ResponseUtil.wrapOrNotFound(entregaService.revisar(id, revision));
    }
}
