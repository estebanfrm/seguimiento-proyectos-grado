package co.edu.sgpg.service;

import co.edu.sgpg.domain.*; // for static metamodels
import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.repository.EntregaRepository;
import co.edu.sgpg.service.criteria.EntregaCriteria;
import co.edu.sgpg.service.dto.EntregaDTO;
import co.edu.sgpg.service.mapper.EntregaMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Entrega} entities in the database.
 * The main input is a {@link EntregaCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link EntregaDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class EntregaQueryService extends QueryService<Entrega> {

    private static final Logger LOG = LoggerFactory.getLogger(EntregaQueryService.class);

    private final EntregaRepository entregaRepository;

    private final EntregaMapper entregaMapper;

    public EntregaQueryService(EntregaRepository entregaRepository, EntregaMapper entregaMapper) {
        this.entregaRepository = entregaRepository;
        this.entregaMapper = entregaMapper;
    }

    /**
     * Return a {@link Page} of {@link EntregaDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EntregaDTO> findByCriteria(EntregaCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Entrega> specification = createSpecification(criteria);
        return entregaRepository.findAll(specification, page).map(entregaMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EntregaCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Entrega> specification = createSpecification(criteria);
        return entregaRepository.count(specification);
    }

    /**
     * Function to convert {@link EntregaCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Entrega> createSpecification(EntregaCriteria criteria) {
        Specification<Entrega> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Entrega_.autor, JoinType.LEFT);
                root.fetch(Entrega_.proyecto, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Entrega_.id),
                    buildSpecification(criteria.getEtapa(), Entrega_.etapa),
                    buildStringSpecification(criteria.getTitulo(), Entrega_.titulo),
                    buildStringSpecification(criteria.getUrlDocumento(), Entrega_.urlDocumento),
                    buildRangeSpecification(criteria.getVersion(), Entrega_.version),
                    buildRangeSpecification(criteria.getFechaEntrega(), Entrega_.fechaEntrega),
                    buildSpecification(criteria.getEstado(), Entrega_.estado),
                    buildSpecification(criteria.getObservacionesId(), root ->
                        root.join(Entrega_.observacioneses, JoinType.LEFT).get(Observacion_.id)
                    ),
                    buildSpecification(criteria.getAutorId(), root -> root.join(Entrega_.autor, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getProyectoId(), root -> root.join(Entrega_.proyecto, JoinType.LEFT).get(ProyectoGrado_.id))
                )
            );
        }
        return specification;
    }
}
