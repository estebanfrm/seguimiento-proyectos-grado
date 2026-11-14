package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.service.ProyectoGradoService;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.mapper.ProyectoGradoMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link co.edu.sgpg.domain.ProyectoGrado}.
 */
@Service
@Transactional
public class ProyectoGradoServiceImpl implements ProyectoGradoService {

    private static final Logger LOG = LoggerFactory.getLogger(ProyectoGradoServiceImpl.class);

    private final ProyectoGradoRepository proyectoGradoRepository;

    private final ProyectoGradoMapper proyectoGradoMapper;

    public ProyectoGradoServiceImpl(ProyectoGradoRepository proyectoGradoRepository, ProyectoGradoMapper proyectoGradoMapper) {
        this.proyectoGradoRepository = proyectoGradoRepository;
        this.proyectoGradoMapper = proyectoGradoMapper;
    }

    @Override
    public ProyectoGradoDTO save(ProyectoGradoDTO proyectoGradoDTO) {
        LOG.debug("Request to save ProyectoGrado : {}", proyectoGradoDTO);
        ProyectoGrado proyectoGrado = proyectoGradoMapper.toEntity(proyectoGradoDTO);
        proyectoGrado = proyectoGradoRepository.save(proyectoGrado);
        return proyectoGradoMapper.toDto(proyectoGrado);
    }

    @Override
    public ProyectoGradoDTO update(ProyectoGradoDTO proyectoGradoDTO) {
        LOG.debug("Request to update ProyectoGrado : {}", proyectoGradoDTO);
        ProyectoGrado proyectoGrado = proyectoGradoMapper.toEntity(proyectoGradoDTO);
        proyectoGrado = proyectoGradoRepository.save(proyectoGrado);
        return proyectoGradoMapper.toDto(proyectoGrado);
    }

    @Override
    public Optional<ProyectoGradoDTO> partialUpdate(ProyectoGradoDTO proyectoGradoDTO) {
        LOG.debug("Request to partially update ProyectoGrado : {}", proyectoGradoDTO);

        return proyectoGradoRepository
            .findById(proyectoGradoDTO.getId())
            .map(existingProyectoGrado -> {
                proyectoGradoMapper.partialUpdate(existingProyectoGrado, proyectoGradoDTO);

                return existingProyectoGrado;
            })
            .map(proyectoGradoRepository::save)
            .map(proyectoGradoMapper::toDto);
    }

    public Page<ProyectoGradoDTO> findAllWithEagerRelationships(Pageable pageable) {
        return proyectoGradoRepository.findAllWithEagerRelationships(pageable).map(proyectoGradoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProyectoGradoDTO> findOne(Long id) {
        LOG.debug("Request to get ProyectoGrado : {}", id);
        return proyectoGradoRepository.findOneWithEagerRelationships(id).map(proyectoGradoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete ProyectoGrado : {}", id);
        proyectoGradoRepository.deleteById(id);
    }
}
