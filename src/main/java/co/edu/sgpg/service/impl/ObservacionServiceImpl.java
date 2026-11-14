package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.Observacion;
import co.edu.sgpg.repository.ObservacionRepository;
import co.edu.sgpg.service.ObservacionService;
import co.edu.sgpg.service.dto.ObservacionDTO;
import co.edu.sgpg.service.mapper.ObservacionMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link co.edu.sgpg.domain.Observacion}.
 */
@Service
@Transactional
public class ObservacionServiceImpl implements ObservacionService {

    private static final Logger LOG = LoggerFactory.getLogger(ObservacionServiceImpl.class);

    private final ObservacionRepository observacionRepository;

    private final ObservacionMapper observacionMapper;

    public ObservacionServiceImpl(ObservacionRepository observacionRepository, ObservacionMapper observacionMapper) {
        this.observacionRepository = observacionRepository;
        this.observacionMapper = observacionMapper;
    }

    @Override
    public ObservacionDTO save(ObservacionDTO observacionDTO) {
        LOG.debug("Request to save Observacion : {}", observacionDTO);
        Observacion observacion = observacionMapper.toEntity(observacionDTO);
        observacion = observacionRepository.save(observacion);
        return observacionMapper.toDto(observacion);
    }

    @Override
    public ObservacionDTO update(ObservacionDTO observacionDTO) {
        LOG.debug("Request to update Observacion : {}", observacionDTO);
        Observacion observacion = observacionMapper.toEntity(observacionDTO);
        observacion = observacionRepository.save(observacion);
        return observacionMapper.toDto(observacion);
    }

    @Override
    public Optional<ObservacionDTO> partialUpdate(ObservacionDTO observacionDTO) {
        LOG.debug("Request to partially update Observacion : {}", observacionDTO);

        return observacionRepository
            .findById(observacionDTO.getId())
            .map(existingObservacion -> {
                observacionMapper.partialUpdate(existingObservacion, observacionDTO);

                return existingObservacion;
            })
            .map(observacionRepository::save)
            .map(observacionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionDTO> findAll() {
        LOG.debug("Request to get all Observacions");
        return observacionRepository.findAll().stream().map(observacionMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public List<ObservacionDTO> findAllWithEagerRelationships() {
        return observacionRepository
            .findAllWithEagerRelationships()
            .stream()
            .map(observacionMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ObservacionDTO> findOne(Long id) {
        LOG.debug("Request to get Observacion : {}", id);
        return observacionRepository.findOneWithEagerRelationships(id).map(observacionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Observacion : {}", id);
        observacionRepository.deleteById(id);
    }
}
