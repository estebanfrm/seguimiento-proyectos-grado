package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.security.AuthoritiesConstants;
import co.edu.sgpg.service.*;
import co.edu.sgpg.service.dto.AsignacionesDTO;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.mapper.ProyectoGradoMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProyectoGradoServiceImpl implements ProyectoGradoService {

    private final ProyectoGradoRepository repositorio;
    private final ProyectoGradoMapper mapper;
    private final ProyectoAccessService acceso;
    private final PermisosEntidadService permisos;
    private final VisibilidadProyectoService visibilidad;
    private final AvanceService avance;
    private final Clock clock;
    private final UserRepository usuarios;

    public ProyectoGradoServiceImpl(
        ProyectoGradoRepository repositorio,
        ProyectoGradoMapper mapper,
        ProyectoAccessService acceso,
        PermisosEntidadService permisos,
        VisibilidadProyectoService visibilidad,
        AvanceService avance,
        Clock clock,
        UserRepository usuarios
    ) {
        this.repositorio = repositorio;
        this.mapper = mapper;
        this.acceso = acceso;
        this.permisos = permisos;
        this.visibilidad = visibilidad;
        this.avance = avance;
        this.clock = clock;
        this.usuarios = usuarios;
    }

    @Override
    public ProyectoGradoDTO save(ProyectoGradoDTO dto) {
        permisos.creadorProyecto();
        ProyectoGrado proyecto = mapper.toEntity(dto);
        proyecto.setEtapaActual(Etapa.PROPUESTA);
        proyecto.setEstado(EstadoProyecto.ACTIVO);
        proyecto.setPorcentajeAvance(0);
        proyecto.setFechaRegistro(Instant.now(clock));
        proyecto.setFechaActualizacion(Instant.now(clock));
        if (!acceso.esAdmin()) {
            proyecto.setEstudiantes(new HashSet<>(Set.of(permisos.usuarioActual())));
            proyecto.setTutor(null);
            proyecto.setJurados(new HashSet<>());
        }
        if (proyecto.getEstudiantes() == null) proyecto.setEstudiantes(new HashSet<>());
        if (proyecto.getJurados() == null) proyecto.setJurados(new HashSet<>());
        proyecto = repositorio.save(proyecto);
        // La creación siempre empieza en cero; recalcular solo si hay asignaciones administrativas.
        if (acceso.esAdmin() && (proyecto.getTutor() != null || !proyecto.getJurados().isEmpty())) {
            avance.recalcular(proyecto.getId());
        }
        return mapper.toDto(proyecto);
    }

    @Override
    public ProyectoGradoDTO update(ProyectoGradoDTO dto) {
        return modificar(dto, false).orElseThrow();
    }

    @Override
    public Optional<ProyectoGradoDTO> partialUpdate(ProyectoGradoDTO dto) {
        return modificar(dto, true);
    }

    private Optional<ProyectoGradoDTO> modificar(ProyectoGradoDTO dto, boolean parcial) {
        return repositorio.findById(dto.getId()).map(proyecto -> {
            if (!acceso.esAdmin()) {
                permisos.estudiante(proyecto.getId());
                if (proyecto.getEtapaActual() != Etapa.PROPUESTA) {
                    throw new AccessDeniedException("El estudiante solo puede editar su propuesta");
                }
            }
            if (!parcial || dto.getTitulo() != null) proyecto.setTitulo(dto.getTitulo());
            if (!parcial || dto.getResumen() != null) proyecto.setResumen(dto.getResumen());
            if (acceso.esAdmin()) {
                if (!parcial || dto.getPrograma() != null) proyecto.setPrograma(dto.getPrograma());
                ProyectoGrado relaciones = mapper.toEntity(dto);
                if (!parcial || dto.getTutor() != null) proyecto.setTutor(relaciones.getTutor());
                if (dto.getEstudiantes() != null) proyecto.setEstudiantes(relaciones.getEstudiantes());
                if (dto.getJurados() != null) proyecto.setJurados(relaciones.getJurados());
            }
            // etapaActual, estado, avance y fechaRegistro pertenecen al workflow/servidor.
            proyecto.setFechaActualizacion(Instant.now(clock));
            proyecto = repositorio.save(proyecto);
            avance.recalcular(proyecto.getId());
            return mapper.toDto(proyecto);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProyectoGradoDTO> findAllWithEagerRelationships(Pageable pageable) {
        return repositorio
            .fetchBagRelationships(repositorio.findAll(visibilidad.para(root -> root.get("id")), pageable))
            .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProyectoGradoDTO> findOne(Long id) {
        return repositorio.findOneWithEagerRelationships(id).map(proyecto -> {
            acceso.verificarPuedeVer(id);
            return mapper.toDto(proyecto);
        });
    }

    @Override
    public void delete(Long id) {
        permisos.admin();
        repositorio.deleteById(id);
    }

    @Override
    public ProyectoGradoDTO actualizarAsignaciones(Long id, AsignacionesDTO asignaciones) {
        permisos.admin();
        ProyectoGrado proyecto = repositorio
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El proyecto no existe"));
        User tutor = asignaciones.tutorId() == null ? null : usuarioConRol(asignaciones.tutorId(), AuthoritiesConstants.TUTOR);
        Set<User> jurados = asignaciones
            .juradoIds()
            .stream()
            .map(usuarioId -> usuarioConRol(usuarioId, AuthoritiesConstants.JURADO))
            .collect(Collectors.toCollection(HashSet::new));
        Set<User> estudiantes = asignaciones
            .estudianteIds()
            .stream()
            .map(usuarioId -> usuarioConRol(usuarioId, AuthoritiesConstants.ESTUDIANTE))
            .collect(Collectors.toCollection(HashSet::new));
        proyecto.setTutor(tutor);
        proyecto.setJurados(jurados);
        proyecto.setEstudiantes(estudiantes);
        proyecto.setFechaActualizacion(Instant.now(clock));
        proyecto = repositorio.save(proyecto);
        avance.recalcular(proyecto.getId());
        return mapper.toDto(proyecto);
    }

    private User usuarioConRol(Long id, String rol) {
        return Optional.ofNullable(id)
            .flatMap(usuarios::findById)
            .filter(User::isActivated)
            .filter(usuario ->
                usuario
                    .getAuthorities()
                    .stream()
                    .anyMatch(authority -> rol.equals(authority.getName()))
            )
            .orElseThrow(() ->
                new ReglaNegocioException("USUARIO_ROL_INVALIDO", "El usuario " + id + " debe existir, estar activo y tener el rol " + rol)
            );
    }
}
