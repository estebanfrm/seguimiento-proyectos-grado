package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.repository.EntregaRepository;
import co.edu.sgpg.service.EntregaService;
import co.edu.sgpg.service.dto.EntregaDTO;
import co.edu.sgpg.service.mapper.EntregaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link co.edu.sgpg.domain.Entrega}.
 */
@Service
@Transactional
public class EntregaServiceImpl implements EntregaService {

    private static final Logger LOG = LoggerFactory.getLogger(EntregaServiceImpl.class);

    private final EntregaRepository entregaRepository;

    private final EntregaMapper entregaMapper;

    public EntregaServiceImpl(EntregaRepository entregaRepository, EntregaMapper entregaMapper) {
        this.entregaRepository = entregaRepository;
        this.entregaMapper = entregaMapper;
    }

    @Override
    public EntregaDTO save(EntregaDTO entregaDTO) {
        LOG.debug("Request to save Entrega : {}", entregaDTO);
        Entrega entrega = entregaMapper.toEntity(entregaDTO);
        entrega = entregaRepository.save(entrega);
        return entregaMapper.toDto(entrega);
    }

    @Override
    public EntregaDTO update(EntregaDTO entregaDTO) {
        LOG.debug("Request to update Entrega : {}", entregaDTO);
        Entrega entrega = entregaMapper.toEntity(entregaDTO);
        entrega = entregaRepository.save(entrega);
        return entregaMapper.toDto(entrega);
    }

    @Override
    public Optional<EntregaDTO> partialUpdate(EntregaDTO entregaDTO) {
        LOG.debug("Request to partially update Entrega : {}", entregaDTO);

        return entregaRepository
            .findById(entregaDTO.getId())
            .map(existingEntrega -> {
                entregaMapper.partialUpdate(existingEntrega, entregaDTO);

                return existingEntrega;
            })
            .map(entregaRepository::save)
            .map(entregaMapper::toDto);
    }

    public Page<EntregaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return entregaRepository.findAllWithEagerRelationships(pageable).map(entregaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntregaDTO> findOne(Long id) {
        LOG.debug("Request to get Entrega : {}", id);
        return entregaRepository.findOneWithEagerRelationships(id).map(entregaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Entrega : {}", id);
        entregaRepository.deleteById(id);
    }
}
