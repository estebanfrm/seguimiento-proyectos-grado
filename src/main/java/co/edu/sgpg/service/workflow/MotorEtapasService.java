package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.HistorialEtapa;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.HistorialEtapaRepository;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.service.AvanceService;
import co.edu.sgpg.service.ProyectoAccessService;
import co.edu.sgpg.service.ReglaNegocioException;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import co.edu.sgpg.service.dto.workflow.RequisitosTransicionDTO;
import co.edu.sgpg.service.mapper.ProyectoGradoMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Único punto de mutación de las etapas y del estado académico del proyecto. */
@Service
@Transactional
public class MotorEtapasService {

    private final ProyectoGradoRepository proyectos;
    private final ProyectoAccessService acceso;
    private final EvaluadorRequisitos evaluador;
    private final Requisitos requisitos;
    private final AvanceService avance;
    private final HistorialEtapaRepository historial;
    private final UserRepository usuarios;
    private final ProyectoGradoMapper mapper;
    private final Clock clock;

    public MotorEtapasService(
        ProyectoGradoRepository proyectos,
        ProyectoAccessService acceso,
        EvaluadorRequisitos evaluador,
        Requisitos requisitos,
        AvanceService avance,
        HistorialEtapaRepository historial,
        UserRepository usuarios,
        ProyectoGradoMapper mapper,
        Clock clock
    ) {
        this.proyectos = proyectos;
        this.acceso = acceso;
        this.evaluador = evaluador;
        this.requisitos = requisitos;
        this.avance = avance;
        this.historial = historial;
        this.usuarios = usuarios;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public RequisitosTransicionDTO requisitos(Long proyectoId) {
        ProyectoGrado proyecto = obtenerProyecto(proyectoId);
        acceso.verificarPuedeVer(proyectoId);
        return evaluador.evaluar(proyecto);
    }

    public ProyectoGradoDTO avanzar(Long proyectoId, String comentario) {
        ProyectoGrado proyecto = obtenerProyecto(proyectoId);
        acceso.verificarTutorOAdmin(proyectoId);
        if (proyecto.getEstado() != EstadoProyecto.ACTIVO) {
            throw new ReglaNegocioException("PROYECTO_NO_ACTIVO", "Solo se pueden avanzar proyectos activos");
        }
        if (proyecto.getEtapaActual() == Etapa.FINALIZADO) {
            throw new ReglaNegocioException("ETAPA_FINAL", "El proyecto ya está en la etapa final");
        }
        RequisitosTransicionDTO transicion = evaluador.evaluar(proyecto);
        List<String> pendientes = transicion
            .requisitos()
            .stream()
            .filter(r -> !r.cumplido())
            .map(RequisitoDTO::codigo)
            .toList();
        if (!pendientes.isEmpty()) {
            throw new ReglaNegocioException("REQUISITOS_PENDIENTES", "Hay requisitos pendientes para avanzar de etapa", pendientes);
        }
        Etapa origen = proyecto.getEtapaActual();
        User usuario = usuarioActual();
        if (transicion.etapaSiguiente() == Etapa.FINALIZADO) {
            proyecto.setEstado(requisitos.resultadoAprobatorio(proyecto) ? EstadoProyecto.APROBADO : EstadoProyecto.REPROBADO);
        }
        proyecto.setEtapaActual(transicion.etapaSiguiente());
        registrarCambio(proyecto, origen, usuario, comentario);
        return mapper.toDto(proyecto);
    }

    public ProyectoGradoDTO cancelar(Long proyectoId, String comentario) {
        ProyectoGrado proyecto = obtenerProyecto(proyectoId);
        if (!acceso.esAdmin()) {
            throw new AccessDeniedException("Solo el coordinador puede cancelar proyectos");
        }
        if (comentario == null || comentario.isBlank()) {
            throw new ReglaNegocioException("COMENTARIO_OBLIGATORIO", "Debe indicar el motivo de la cancelación");
        }
        User usuario = usuarioActual();
        proyecto.setEstado(EstadoProyecto.CANCELADO);
        registrarCambio(proyecto, proyecto.getEtapaActual(), usuario, comentario);
        return mapper.toDto(proyecto);
    }

    private void registrarCambio(ProyectoGrado proyecto, Etapa origen, User usuario, String comentario) {
        Instant ahora = Instant.now(clock);
        proyecto.setFechaActualizacion(ahora);
        proyectos.save(proyecto);
        historial.save(
            new HistorialEtapa()
                .proyecto(proyecto)
                .etapaOrigen(origen)
                .etapaDestino(proyecto.getEtapaActual())
                .fecha(ahora)
                .usuario(usuario)
                .comentario(comentario)
        );
        proyecto.setPorcentajeAvance(avance.recalcular(proyecto.getId()));
    }

    private User usuarioActual() {
        return acceso
            .loginActual()
            .flatMap(usuarios::findOneByLogin)
            .orElseThrow(() -> new AccessDeniedException("No se encontró el usuario actual"));
    }

    private ProyectoGrado obtenerProyecto(Long id) {
        return proyectos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El proyecto no existe"));
    }
}
