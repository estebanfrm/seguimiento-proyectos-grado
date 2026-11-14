package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.FechaLimiteEtapa;
import co.edu.sgpg.repository.FechaLimiteEtapaRepository;
import co.edu.sgpg.service.FechaLimiteEtapaService;
import co.edu.sgpg.service.dto.FechaLimiteEtapaDTO;
import co.edu.sgpg.service.mapper.FechaLimiteEtapaMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link co.edu.sgpg.domain.FechaLimiteEtapa}.
 */
@Service
@Transactional
public class FechaLimiteEtapaServiceImpl implements FechaLimiteEtapaService {

    private static final Logger LOG = LoggerFactory.getLogger(FechaLimiteEtapaServiceImpl.class);

    private final FechaLimiteEtapaRepository fechaLimiteEtapaRepository;

    private final FechaLimiteEtapaMapper fechaLimiteEtapaMapper;

    public FechaLimiteEtapaServiceImpl(
        FechaLimiteEtapaRepository fechaLimiteEtapaRepository,
        FechaLimiteEtapaMapper fechaLimiteEtapaMapper
    ) {
        this.fechaLimiteEtapaRepository = fechaLimiteEtapaRepository;
        this.fechaLimiteEtapaMapper = fechaLimiteEtapaMapper;
    }

    @Override
    public FechaLimiteEtapaDTO save(FechaLimiteEtapaDTO fechaLimiteEtapaDTO) {
        LOG.debug("Request to save FechaLimiteEtapa : {}", fechaLimiteEtapaDTO);
        FechaLimiteEtapa fechaLimiteEtapa = fechaLimiteEtapaMapper.toEntity(fechaLimiteEtapaDTO);
        fechaLimiteEtapa = fechaLimiteEtapaRepository.save(fechaLimiteEtapa);
        return fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);
    }

    @Override
    public FechaLimiteEtapaDTO update(FechaLimiteEtapaDTO fechaLimiteEtapaDTO) {
        LOG.debug("Request to update FechaLimiteEtapa : {}", fechaLimiteEtapaDTO);
        FechaLimiteEtapa fechaLimiteEtapa = fechaLimiteEtapaMapper.toEntity(fechaLimiteEtapaDTO);
        fechaLimiteEtapa = fechaLimiteEtapaRepository.save(fechaLimiteEtapa);
        return fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);
    }

    @Override
    public Optional<FechaLimiteEtapaDTO> partialUpdate(FechaLimiteEtapaDTO fechaLimiteEtapaDTO) {
        LOG.debug("Request to partially update FechaLimiteEtapa : {}", fechaLimiteEtapaDTO);

        return fechaLimiteEtapaRepository
            .findById(fechaLimiteEtapaDTO.getId())
            .map(existingFechaLimiteEtapa -> {
                fechaLimiteEtapaMapper.partialUpdate(existingFechaLimiteEtapa, fechaLimiteEtapaDTO);

                return existingFechaLimiteEtapa;
            })
            .map(fechaLimiteEtapaRepository::save)
            .map(fechaLimiteEtapaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FechaLimiteEtapaDTO> findAll() {
        LOG.debug("Request to get all FechaLimiteEtapas");
        return fechaLimiteEtapaRepository
            .findAll()
            .stream()
            .map(fechaLimiteEtapaMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public List<FechaLimiteEtapaDTO> findAllWithEagerRelationships() {
        return fechaLimiteEtapaRepository
            .findAllWithEagerRelationships()
            .stream()
            .map(fechaLimiteEtapaMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FechaLimiteEtapaDTO> findOne(Long id) {
        LOG.debug("Request to get FechaLimiteEtapa : {}", id);
        return fechaLimiteEtapaRepository.findOneWithEagerRelationships(id).map(fechaLimiteEtapaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete FechaLimiteEtapa : {}", id);
        fechaLimiteEtapaRepository.deleteById(id);
    }
}
