package co.edu.sgpg.service;

import co.edu.sgpg.domain.*; // for static metamodels
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.service.criteria.ProyectoGradoCriteria;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.mapper.ProyectoGradoMapper;
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
 * Service for executing complex queries for {@link ProyectoGrado} entities in the database.
 * The main input is a {@link ProyectoGradoCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ProyectoGradoDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ProyectoGradoQueryService extends QueryService<ProyectoGrado> {

    private static final Logger LOG = LoggerFactory.getLogger(ProyectoGradoQueryService.class);

    private final ProyectoGradoRepository proyectoGradoRepository;

    private final ProyectoGradoMapper proyectoGradoMapper;

    public ProyectoGradoQueryService(ProyectoGradoRepository proyectoGradoRepository, ProyectoGradoMapper proyectoGradoMapper) {
        this.proyectoGradoRepository = proyectoGradoRepository;
        this.proyectoGradoMapper = proyectoGradoMapper;
    }

    /**
     * Return a {@link Page} of {@link ProyectoGradoDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ProyectoGradoDTO> findByCriteria(ProyectoGradoCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ProyectoGrado> specification = createSpecification(criteria);
        return proyectoGradoRepository
            .fetchBagRelationships(proyectoGradoRepository.findAll(specification, page))
            .map(proyectoGradoMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ProyectoGradoCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<ProyectoGrado> specification = createSpecification(criteria);
        return proyectoGradoRepository.count(specification);
    }

    /**
     * Function to convert {@link ProyectoGradoCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ProyectoGrado> createSpecification(ProyectoGradoCriteria criteria) {
        Specification<ProyectoGrado> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(ProyectoGrado_.tutor, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), ProyectoGrado_.id),
                    buildStringSpecification(criteria.getTitulo(), ProyectoGrado_.titulo),
                    buildStringSpecification(criteria.getPrograma(), ProyectoGrado_.programa),
                    buildSpecification(criteria.getEtapaActual(), ProyectoGrado_.etapaActual),
                    buildSpecification(criteria.getEstado(), ProyectoGrado_.estado),
                    buildRangeSpecification(criteria.getPorcentajeAvance(), ProyectoGrado_.porcentajeAvance),
                    buildRangeSpecification(criteria.getFechaRegistro(), ProyectoGrado_.fechaRegistro),
                    buildRangeSpecification(criteria.getFechaActualizacion(), ProyectoGrado_.fechaActualizacion),
                    buildSpecification(criteria.getEntregaId(), root -> root.join(ProyectoGrado_.entregas, JoinType.LEFT).get(Entrega_.id)),
                    buildSpecification(criteria.getFechaLimiteId(), root ->
                        root.join(ProyectoGrado_.fechaLimites, JoinType.LEFT).get(FechaLimiteEtapa_.id)
                    ),
                    buildSpecification(criteria.getEvaluacionId(), root ->
                        root.join(ProyectoGrado_.evaluacions, JoinType.LEFT).get(Evaluacion_.id)
                    ),
                    buildSpecification(criteria.getHistorialId(), root ->
                        root.join(ProyectoGrado_.historials, JoinType.LEFT).get(HistorialEtapa_.id)
                    ),
                    buildSpecification(criteria.getTutorId(), root -> root.join(ProyectoGrado_.tutor, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getEstudianteId(), root ->
                        root.join(ProyectoGrado_.estudiantes, JoinType.LEFT).get(User_.id)
                    ),
                    buildSpecification(criteria.getJuradoId(), root -> root.join(ProyectoGrado_.jurados, JoinType.LEFT).get(User_.id))
                )
            );
        }
        return specification;
    }
}
