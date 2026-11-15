package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.EvaluacionRepository;
import co.edu.sgpg.service.*;
import co.edu.sgpg.service.dto.EvaluacionDTO;
import co.edu.sgpg.service.mapper.EvaluacionMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionRepository repositorio;
    private final EvaluacionMapper mapper;
    private final ProyectoAccessService acceso;
    private final PermisosEntidadService permisos;
    private final AvanceService avance;
    private final Clock clock;

    public EvaluacionServiceImpl(
        EvaluacionRepository repositorio,
        EvaluacionMapper mapper,
        ProyectoAccessService acceso,
        PermisosEntidadService permisos,
        AvanceService avance,
        Clock clock
    ) {
        this.repositorio = repositorio;
        this.mapper = mapper;
        this.acceso = acceso;
        this.permisos = permisos;
        this.avance = avance;
        this.clock = clock;
    }

    @Override
    public EvaluacionDTO save(EvaluacionDTO dto) {
        Long proyectoId = dto.getProyecto() == null ? null : dto.getProyecto().getId();
        permisos.jurado(proyectoId);
        ProyectoGrado proyecto = permisos.proyecto(proyectoId);
        Etapa etapa = proyecto.getEtapaActual();
        if (etapa != Etapa.ANTEPROYECTO && etapa != Etapa.SUSTENTACION) {
            throw new ReglaNegocioException("ETAPA_NO_EVALUABLE", "La etapa actual no admite evaluaciones");
        }
        var jurado = permisos.usuarioActual();
        if (repositorio.existsByProyectoIdAndEtapaAndJuradoLogin(proyectoId, etapa, jurado.getLogin())) {
            throw new ReglaNegocioException("EVALUACION_DUPLICADA", "El jurado ya evaluó esta etapa");
        }
        Evaluacion evaluacion = mapper.toEntity(dto);
        evaluacion.setProyecto(proyecto);
        evaluacion.setEtapa(etapa);
        evaluacion.setJurado(jurado);
        evaluacion.setFechaEvaluacion(Instant.now(clock));
        return guardar(evaluacion);
    }

    @Override
    public EvaluacionDTO update(EvaluacionDTO dto) {
        permisos.admin();
        return modificar(dto, false).orElseThrow();
    }

    @Override
    public Optional<EvaluacionDTO> partialUpdate(EvaluacionDTO dto) {
        permisos.admin();
        return modificar(dto, true);
    }

    private Optional<EvaluacionDTO> modificar(EvaluacionDTO dto, boolean parcial) {
        return repositorio.findById(dto.getId()).map(evaluacion -> {
            if (!parcial || dto.getCalificacion() != null) evaluacion.setCalificacion(dto.getCalificacion());
            if (!parcial || dto.getConcepto() != null) evaluacion.setConcepto(dto.getConcepto());
            if (!parcial || dto.getComentarios() != null) evaluacion.setComentarios(dto.getComentarios());
            return guardar(evaluacion);
        });
    }

    private EvaluacionDTO guardar(Evaluacion evaluacion) {
        if (
            evaluacion.getCalificacion() == null ||
            evaluacion.getConcepto() == null ||
            evaluacion.getCalificacion().compareTo(new BigDecimal("3.0")) < 0 != (evaluacion.getConcepto() == Concepto.REPROBADO)
        ) {
            throw new ReglaNegocioException("CONCEPTO_INCOHERENTE", "La calificación y el concepto no son coherentes");
        }
        evaluacion = repositorio.save(evaluacion);
        avance.recalcular(evaluacion.getProyecto().getId());
        return mapper.toDto(evaluacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionDTO> findAll() {
        var visibles = acceso.esAdmin()
            ? repositorio.findAllWithEagerRelationships()
            : repositorio.findVisiblesPorLogin(acceso.loginActual().orElse(""));
        return visibles.stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionDTO> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EvaluacionDTO> findOne(Long id) {
        return repositorio.findOneWithEagerRelationships(id).map(evaluacion -> {
            acceso.verificarPuedeVer(evaluacion.getProyecto().getId());
            return mapper.toDto(evaluacion);
        });
    }

    @Override
    public void delete(Long id) {
        permisos.admin();
        repositorio.findById(id).ifPresent(evaluacion -> {
            Long proyectoId = evaluacion.getProyecto().getId();
            repositorio.delete(evaluacion);
            avance.recalcular(proyectoId);
        });
    }
}
