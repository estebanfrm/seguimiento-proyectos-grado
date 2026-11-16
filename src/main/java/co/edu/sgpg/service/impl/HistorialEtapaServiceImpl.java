package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.HistorialEtapa;
import co.edu.sgpg.repository.HistorialEtapaRepository;
import co.edu.sgpg.service.HistorialEtapaService;
import co.edu.sgpg.service.ProyectoAccessService;
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

    private final ProyectoAccessService acceso;

    public HistorialEtapaServiceImpl(
        HistorialEtapaRepository historialEtapaRepository,
        HistorialEtapaMapper historialEtapaMapper,
        ProyectoAccessService acceso
    ) {
        this.historialEtapaRepository = historialEtapaRepository;
        this.historialEtapaMapper = historialEtapaMapper;
        this.acceso = acceso;
    }

    @Override
    public HistorialEtapaDTO save(HistorialEtapaDTO historialEtapaDTO) {
        throw new org.springframework.security.access.AccessDeniedException("El historial es de solo lectura");
    }

    @Override
    public HistorialEtapaDTO update(HistorialEtapaDTO historialEtapaDTO) {
        throw new org.springframework.security.access.AccessDeniedException("El historial es de solo lectura");
    }

    @Override
    public Optional<HistorialEtapaDTO> partialUpdate(HistorialEtapaDTO historialEtapaDTO) {
        throw new org.springframework.security.access.AccessDeniedException("El historial es de solo lectura");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEtapaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all HistorialEtapas");
        return (
            acceso.esAdmin()
                ? historialEtapaRepository.findAllWithEagerRelationships(pageable)
                : historialEtapaRepository.findVisiblesPorLogin(acceso.loginActual().orElse(""), pageable)
        ).map(historialEtapaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEtapaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HistorialEtapaDTO> findOne(Long id) {
        LOG.debug("Request to get HistorialEtapa : {}", id);
        return historialEtapaRepository.findOneWithEagerRelationships(id).map(e -> {
            acceso.verificarPuedeVer(e.getProyecto().getId());
            return historialEtapaMapper.toDto(e);
        });
    }

    @Override
    public void delete(Long id) {
        throw new org.springframework.security.access.AccessDeniedException("El historial es de solo lectura");
    }
}
