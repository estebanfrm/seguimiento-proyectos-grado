package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.repository.EvaluacionRepository;
import co.edu.sgpg.service.EvaluacionService;
import co.edu.sgpg.service.dto.EvaluacionDTO;
import co.edu.sgpg.service.mapper.EvaluacionMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link co.edu.sgpg.domain.Evaluacion}.
 */
@Service
@Transactional
public class EvaluacionServiceImpl implements EvaluacionService {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluacionServiceImpl.class);

    private final EvaluacionRepository evaluacionRepository;

    private final EvaluacionMapper evaluacionMapper;

    public EvaluacionServiceImpl(EvaluacionRepository evaluacionRepository, EvaluacionMapper evaluacionMapper) {
        this.evaluacionRepository = evaluacionRepository;
        this.evaluacionMapper = evaluacionMapper;
    }

    @Override
    public EvaluacionDTO save(EvaluacionDTO evaluacionDTO) {
        LOG.debug("Request to save Evaluacion : {}", evaluacionDTO);
        Evaluacion evaluacion = evaluacionMapper.toEntity(evaluacionDTO);
        evaluacion = evaluacionRepository.save(evaluacion);
        return evaluacionMapper.toDto(evaluacion);
    }

    @Override
    public EvaluacionDTO update(EvaluacionDTO evaluacionDTO) {
        LOG.debug("Request to update Evaluacion : {}", evaluacionDTO);
        Evaluacion evaluacion = evaluacionMapper.toEntity(evaluacionDTO);
        evaluacion = evaluacionRepository.save(evaluacion);
        return evaluacionMapper.toDto(evaluacion);
    }

    @Override
    public Optional<EvaluacionDTO> partialUpdate(EvaluacionDTO evaluacionDTO) {
        LOG.debug("Request to partially update Evaluacion : {}", evaluacionDTO);

        return evaluacionRepository
            .findById(evaluacionDTO.getId())
            .map(existingEvaluacion -> {
                evaluacionMapper.partialUpdate(existingEvaluacion, evaluacionDTO);

                return existingEvaluacion;
            })
            .map(evaluacionRepository::save)
            .map(evaluacionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionDTO> findAll() {
        LOG.debug("Request to get all Evaluacions");
        return evaluacionRepository.findAll().stream().map(evaluacionMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public List<EvaluacionDTO> findAllWithEagerRelationships() {
        return evaluacionRepository
            .findAllWithEagerRelationships()
            .stream()
            .map(evaluacionMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EvaluacionDTO> findOne(Long id) {
        LOG.debug("Request to get Evaluacion : {}", id);
        return evaluacionRepository.findOneWithEagerRelationships(id).map(evaluacionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Evaluacion : {}", id);
        evaluacionRepository.deleteById(id);
    }
}
