package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.HistorialEtapa;
import co.edu.sgpg.repository.HistorialEtapaRepository;
import co.edu.sgpg.service.HistorialEtapaService;
import co.edu.sgpg.service.dto.HistorialEtapaDTO;
import co.edu.sgpg.service.mapper.HistorialEtapaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link co.edu.sgpg.domain.HistorialEtapa}.
 */
@Service
@Transactional
public class HistorialEtapaServiceImpl implements HistorialEtapaService {

    private static final Logger LOG = LoggerFactory.getLogger(HistorialEtapaServiceImpl.class);

    private final HistorialEtapaRepository historialEtapaRepository;

    private final HistorialEtapaMapper historialEtapaMapper;

    public HistorialEtapaServiceImpl(HistorialEtapaRepository historialEtapaRepository, HistorialEtapaMapper historialEtapaMapper) {
        this.historialEtapaRepository = historialEtapaRepository;
        this.historialEtapaMapper = historialEtapaMapper;
    }

    @Override
    public HistorialEtapaDTO save(HistorialEtapaDTO historialEtapaDTO) {
        LOG.debug("Request to save HistorialEtapa : {}", historialEtapaDTO);
        HistorialEtapa historialEtapa = historialEtapaMapper.toEntity(historialEtapaDTO);
        historialEtapa = historialEtapaRepository.save(historialEtapa);
        return historialEtapaMapper.toDto(historialEtapa);
    }

    @Override
    public HistorialEtapaDTO update(HistorialEtapaDTO historialEtapaDTO) {
        LOG.debug("Request to update HistorialEtapa : {}", historialEtapaDTO);
        HistorialEtapa historialEtapa = historialEtapaMapper.toEntity(historialEtapaDTO);
        historialEtapa = historialEtapaRepository.save(historialEtapa);
        return historialEtapaMapper.toDto(historialEtapa);
    }

    @Override
    public Optional<HistorialEtapaDTO> partialUpdate(HistorialEtapaDTO historialEtapaDTO) {
        LOG.debug("Request to partially update HistorialEtapa : {}", historialEtapaDTO);

        return historialEtapaRepository
            .findById(historialEtapaDTO.getId())
            .map(existingHistorialEtapa -> {
                historialEtapaMapper.partialUpdate(existingHistorialEtapa, historialEtapaDTO);

                return existingHistorialEtapa;
            })
            .map(historialEtapaRepository::save)
            .map(historialEtapaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEtapaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all HistorialEtapas");
        return historialEtapaRepository.findAll(pageable).map(historialEtapaMapper::toDto);
    }

    public Page<HistorialEtapaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return historialEtapaRepository.findAllWithEagerRelationships(pageable).map(historialEtapaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HistorialEtapaDTO> findOne(Long id) {
        LOG.debug("Request to get HistorialEtapa : {}", id);
        return historialEtapaRepository.findOneWithEagerRelationships(id).map(historialEtapaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete HistorialEtapa : {}", id);
        historialEtapaRepository.deleteById(id);
    }
}
