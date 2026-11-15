package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.Observacion;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.*;
import co.edu.sgpg.service.*;
import co.edu.sgpg.service.dto.ObservacionDTO;
import co.edu.sgpg.service.mapper.ObservacionMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ObservacionServiceImpl implements ObservacionService {

    private final ObservacionRepository repositorio;
    private final EntregaRepository entregas;
    private final ObservacionMapper mapper;
    private final ProyectoAccessService acceso;
    private final PermisosEntidadService permisos;
    private final AvanceService avance;
    private final Clock clock;

    public ObservacionServiceImpl(
        ObservacionRepository repositorio,
        EntregaRepository entregas,
        ObservacionMapper mapper,
        ProyectoAccessService acceso,
        PermisosEntidadService permisos,
        AvanceService avance,
        Clock clock
    ) {
        this.repositorio = repositorio;
        this.entregas = entregas;
        this.mapper = mapper;
        this.acceso = acceso;
        this.permisos = permisos;
        this.avance = avance;
        this.clock = clock;
    }

    @Override
    public ObservacionDTO save(ObservacionDTO dto) {
        if (dto.getEntrega() == null || dto.getEntrega().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar la entrega");
        }
        Entrega entrega = entregas
            .findById(dto.getEntrega().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entrega inexistente"));
        permisos.tutorOAdmin(entrega.getProyecto().getId());
        Observacion observacion = mapper.toEntity(dto);
        observacion.setEntrega(entrega);
        observacion.setAutor(permisos.usuarioActual());
        observacion.setFechaCreacion(Instant.now(clock));
        observacion.setResuelta(false);
        return guardar(observacion);
    }

    @Override
    public ObservacionDTO update(ObservacionDTO dto) {
        return modificar(dto, false).orElseThrow();
    }

    @Override
    public Optional<ObservacionDTO> partialUpdate(ObservacionDTO dto) {
        return modificar(dto, true);
    }

    private Optional<ObservacionDTO> modificar(ObservacionDTO dto, boolean parcial) {
        return repositorio.findById(dto.getId()).map(observacion -> {
            permisos.tutorOAdmin(observacion.getEntrega().getProyecto().getId());
            if (!parcial || dto.getContenido() != null) observacion.setContenido(dto.getContenido());
            if (!parcial || dto.getTipo() != null) observacion.setTipo(dto.getTipo());
            return guardar(observacion);
        });
    }

    private ObservacionDTO guardar(Observacion observacion) {
        if (observacion.getTipo() == TipoObservacion.CORRECCION_REQUERIDA && !Boolean.TRUE.equals(observacion.getResuelta())) {
            observacion.getEntrega().setEstado(EstadoEntrega.CON_OBSERVACIONES);
            entregas.save(observacion.getEntrega());
        }
        observacion = repositorio.save(observacion);
        avance.recalcular(observacion.getEntrega().getProyecto().getId());
        return mapper.toDto(observacion);
    }

    @Override
    public Optional<ObservacionDTO> resolver(Long id) {
        return repositorio.findById(id).map(observacion -> {
            permisos.tutorOAdmin(observacion.getEntrega().getProyecto().getId());
            observacion.setResuelta(true);
            return guardar(observacion);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionDTO> findAll() {
        var visibles = acceso.esAdmin()
            ? repositorio.findAllWithEagerRelationships()
            : repositorio.findVisiblesPorLogin(acceso.loginActual().orElse(""));
        return visibles.stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionDTO> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ObservacionDTO> findOne(Long id) {
        return repositorio.findOneWithEagerRelationships(id).map(observacion -> {
            acceso.verificarPuedeVer(observacion.getEntrega().getProyecto().getId());
            return mapper.toDto(observacion);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<ObservacionDTO>> findByEntrega(Long entregaId) {
        return entregas.findById(entregaId).map(entrega -> {
            acceso.verificarPuedeVer(entrega.getProyecto().getId());
            return repositorio.findByEntregaIdOrderByFechaCreacionAsc(entregaId).stream().map(mapper::toDto).toList();
        });
    }

    @Override
    public void delete(Long id) {
        permisos.admin();
        repositorio.findById(id).ifPresent(observacion -> {
            Long proyectoId = observacion.getEntrega().getProyecto().getId();
            repositorio.delete(observacion);
            avance.recalcular(proyectoId);
        });
    }
}
