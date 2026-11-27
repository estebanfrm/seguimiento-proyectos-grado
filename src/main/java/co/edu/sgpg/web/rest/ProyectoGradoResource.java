package co.edu.sgpg.web.rest;

import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.security.AuthoritiesConstants;
import co.edu.sgpg.service.ProyectoGradoQueryService;
import co.edu.sgpg.service.ProyectoGradoService;
import co.edu.sgpg.service.criteria.ProyectoGradoCriteria;
import co.edu.sgpg.service.dto.AsignacionesDTO;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link co.edu.sgpg.domain.ProyectoGrado}.
 */
@RestController
@RequestMapping("/api/proyecto-grados")
public class ProyectoGradoResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProyectoGradoResource.class);

    private static final String ENTITY_NAME = "proyectoGrado";

    @Value("${jhipster.clientApp.name:sgpg}")
    private String applicationName;

    private final ProyectoGradoService proyectoGradoService;

    private final ProyectoGradoRepository proyectoGradoRepository;

    private final ProyectoGradoQueryService proyectoGradoQueryService;

    public ProyectoGradoResource(
        ProyectoGradoService proyectoGradoService,
        ProyectoGradoRepository proyectoGradoRepository,
        ProyectoGradoQueryService proyectoGradoQueryService
    ) {
        this.proyectoGradoService = proyectoGradoService;
        this.proyectoGradoRepository = proyectoGradoRepository;
        this.proyectoGradoQueryService = proyectoGradoQueryService;
    }

    /**
     * {@code POST  /proyecto-grados} : Create a new proyectoGrado.
     *
     * @param proyectoGradoDTO the proyectoGradoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new proyectoGradoDTO, or with status {@code 400 (Bad Request)} if the proyectoGrado already has an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "') or hasAuthority('" + AuthoritiesConstants.ESTUDIANTE + "')")
    @PostMapping("")
    public ResponseEntity<ProyectoGradoDTO> createProyectoGrado(@Valid @RequestBody ProyectoGradoDTO proyectoGradoDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save ProyectoGrado : {}", proyectoGradoDTO);
        if (proyectoGradoDTO.getId() != null) {
            throw new BadRequestAlertException("A new proyectoGrado cannot already have an ID", ENTITY_NAME, "idexists");
        }
        proyectoGradoDTO = proyectoGradoService.save(proyectoGradoDTO);
        return ResponseEntity.created(new URI("/api/proyecto-grados/" + proyectoGradoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, proyectoGradoDTO.getId().toString()))
            .body(proyectoGradoDTO);
    }

    /**
     * {@code PUT  /proyecto-grados/:id} : Updates an existing proyectoGrado.
     *
     * @param id the id of the proyectoGradoDTO to save.
     * @param proyectoGradoDTO the proyectoGradoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated proyectoGradoDTO,
     * or with status {@code 400 (Bad Request)} if the proyectoGradoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the proyectoGradoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "') or hasAuthority('" + AuthoritiesConstants.ESTUDIANTE + "')")
    @PutMapping("/{id}")
    public ResponseEntity<ProyectoGradoDTO> updateProyectoGrado(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ProyectoGradoDTO proyectoGradoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ProyectoGrado : {}, {}", id, proyectoGradoDTO);
        if (proyectoGradoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, proyectoGradoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!proyectoGradoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        proyectoGradoDTO = proyectoGradoService.update(proyectoGradoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, proyectoGradoDTO.getId().toString()))
            .body(proyectoGradoDTO);
    }

    /**
     * {@code PATCH  /proyecto-grados/:id} : Partial updates given fields of an existing proyectoGrado, field will ignore if it is null
     *
     * @param id the id of the proyectoGradoDTO to save.
     * @param proyectoGradoDTO the proyectoGradoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated proyectoGradoDTO,
     * or with status {@code 400 (Bad Request)} if the proyectoGradoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the proyectoGradoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the proyectoGradoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "') or hasAuthority('" + AuthoritiesConstants.ESTUDIANTE + "')")
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ProyectoGradoDTO> partialUpdateProyectoGrado(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ProyectoGradoDTO proyectoGradoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partially update ProyectoGrado : {}, {}", id, proyectoGradoDTO);
        if (proyectoGradoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, proyectoGradoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!proyectoGradoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ProyectoGradoDTO> result = proyectoGradoService.partialUpdate(proyectoGradoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, proyectoGradoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /proyecto-grados} : get all the Proyecto Grados.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Proyecto Grados in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ProyectoGradoDTO>> getAllProyectoGrados(
        ProyectoGradoCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get ProyectoGrados by criteria: {}", criteria);

        Page<ProyectoGradoDTO> page = proyectoGradoQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /proyecto-grados/count} : count all the proyectoGrados.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countProyectoGrados(ProyectoGradoCriteria criteria) {
        LOG.debug("REST request to count ProyectoGrados by criteria: {}", criteria);
        return ResponseEntity.ok().body(proyectoGradoQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /proyecto-grados/:id} : get the "id" proyectoGrado.
     *
     * @param id the id of the proyectoGradoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the proyectoGradoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProyectoGradoDTO> getProyectoGrado(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ProyectoGrado : {}", id);
        Optional<ProyectoGradoDTO> proyectoGradoDTO = proyectoGradoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(proyectoGradoDTO);
    }

    /**
     * {@code DELETE  /proyecto-grados/:id} : delete the "id" proyectoGrado.
     *
     * @param id the id of the proyectoGradoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProyectoGrado(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ProyectoGrado : {}", id);
        proyectoGradoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /** Reemplaza las asignaciones completas, permitiendo retirar el tutor con un valor nulo. */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    @PutMapping("/{id}/asignaciones")
    public ResponseEntity<ProyectoGradoDTO> actualizarAsignaciones(
        @PathVariable("id") Long id,
        @Valid @RequestBody AsignacionesDTO asignaciones
    ) {
        ProyectoGradoDTO proyecto = proyectoGradoService.actualizarAsignaciones(id, asignaciones);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .body(proyecto);
    }
}
