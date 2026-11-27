package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.FechaLimiteEtapa;
import co.edu.sgpg.repository.FechaLimiteEtapaRepository;
import co.edu.sgpg.service.AvanceService;
import co.edu.sgpg.service.FechaLimiteEtapaService;
import co.edu.sgpg.service.PermisosEntidadService;
import co.edu.sgpg.service.ProyectoAccessService;
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

    private final ProyectoAccessService acceso;
    private final PermisosEntidadService permisos;
    private final AvanceService avance;

    public FechaLimiteEtapaServiceImpl(
        FechaLimiteEtapaRepository fechaLimiteEtapaRepository,
        FechaLimiteEtapaMapper fechaLimiteEtapaMapper,
        ProyectoAccessService acceso,
        PermisosEntidadService permisos,
        AvanceService avance
    ) {
        this.fechaLimiteEtapaRepository = fechaLimiteEtapaRepository;
        this.fechaLimiteEtapaMapper = fechaLimiteEtapaMapper;
        this.acceso = acceso;
        this.permisos = permisos;
        this.avance = avance;
    }

    @Override
    public FechaLimiteEtapaDTO save(FechaLimiteEtapaDTO fechaLimiteEtapaDTO) {
        permisos.admin();
        LOG.debug("Request to save FechaLimiteEtapa : {}", fechaLimiteEtapaDTO);
        FechaLimiteEtapa fechaLimiteEtapa = fechaLimiteEtapaMapper.toEntity(fechaLimiteEtapaDTO);
        fechaLimiteEtapa.setProyecto(
            permisos.proyecto(fechaLimiteEtapa.getProyecto() == null ? null : fechaLimiteEtapa.getProyecto().getId())
        );
        fechaLimiteEtapa = fechaLimiteEtapaRepository.save(fechaLimiteEtapa);
        avance.recalcular(fechaLimiteEtapa.getProyecto().getId());
        return fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);
    }

    @Override
    public FechaLimiteEtapaDTO update(FechaLimiteEtapaDTO fechaLimiteEtapaDTO) {
        permisos.admin();
        LOG.debug("Request to update FechaLimiteEtapa : {}", fechaLimiteEtapaDTO);
        Long anterior = fechaLimiteEtapaRepository
            .findById(fechaLimiteEtapaDTO.getId())
            .map(e -> e.getProyecto().getId())
            .orElseThrow();
        FechaLimiteEtapa fechaLimiteEtapa = fechaLimiteEtapaMapper.toEntity(fechaLimiteEtapaDTO);
        fechaLimiteEtapa.setProyecto(
            permisos.proyecto(fechaLimiteEtapa.getProyecto() == null ? null : fechaLimiteEtapa.getProyecto().getId())
        );
        fechaLimiteEtapa = fechaLimiteEtapaRepository.save(fechaLimiteEtapa);
        avance.recalcular(anterior);
        if (!anterior.equals(fechaLimiteEtapa.getProyecto().getId())) avance.recalcular(fechaLimiteEtapa.getProyecto().getId());
        return fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);
    }

    @Override
    public Optional<FechaLimiteEtapaDTO> partialUpdate(FechaLimiteEtapaDTO fechaLimiteEtapaDTO) {
        permisos.admin();
        LOG.debug("Request to partially update FechaLimiteEtapa : {}", fechaLimiteEtapaDTO);

        return fechaLimiteEtapaRepository
            .findById(fechaLimiteEtapaDTO.getId())
            .map(existingFechaLimiteEtapa -> {
                var proyecto = existingFechaLimiteEtapa.getProyecto();
                fechaLimiteEtapaMapper.partialUpdate(existingFechaLimiteEtapa, fechaLimiteEtapaDTO);
                existingFechaLimiteEtapa.setProyecto(proyecto);

                return existingFechaLimiteEtapa;
            })
            .map(fechaLimiteEtapaRepository::save)
            .map(e -> {
                avance.recalcular(e.getProyecto().getId());
                return e;
            })
            .map(fechaLimiteEtapaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FechaLimiteEtapaDTO> findAll() {
        LOG.debug("Request to get all FechaLimiteEtapas");
        return (
            acceso.esAdmin()
                ? fechaLimiteEtapaRepository.findAllWithEagerRelationships()
                : fechaLimiteEtapaRepository.findVisiblesPorLogin(acceso.loginActual().orElse(""))
        )
            .stream()
            .map(fechaLimiteEtapaMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FechaLimiteEtapaDTO> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FechaLimiteEtapaDTO> findOne(Long id) {
        LOG.debug("Request to get FechaLimiteEtapa : {}", id);
        return fechaLimiteEtapaRepository.findOneWithEagerRelationships(id).map(e -> {
            acceso.verificarPuedeVer(e.getProyecto().getId());
            return fechaLimiteEtapaMapper.toDto(e);
        });
    }

    @Override
    public void delete(Long id) {
        permisos.admin();
        LOG.debug("Request to delete FechaLimiteEtapa : {}", id);
        fechaLimiteEtapaRepository.findById(id).ifPresent(e -> {
            Long proyectoId = e.getProyecto().getId();
            fechaLimiteEtapaRepository.delete(e);
            avance.recalcular(proyectoId);
        });
    }
}
