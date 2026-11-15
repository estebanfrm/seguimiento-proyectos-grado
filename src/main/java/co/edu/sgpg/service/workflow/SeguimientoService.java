package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.domain.FechaLimiteEtapa;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.EntregaRepository;
import co.edu.sgpg.repository.EvaluacionRepository;
import co.edu.sgpg.repository.FechaLimiteEtapaRepository;
import co.edu.sgpg.repository.HistorialEtapaRepository;
import co.edu.sgpg.repository.ObservacionRepository;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.service.ProyectoAccessService;
import co.edu.sgpg.service.dto.workflow.EntregaResumenDTO;
import co.edu.sgpg.service.dto.workflow.EtapaSeguimientoDTO;
import co.edu.sgpg.service.dto.workflow.SeguimientoProyectoDTO;
import co.edu.sgpg.service.mapper.EvaluacionMapper;
import co.edu.sgpg.service.mapper.HistorialEtapaMapper;
import co.edu.sgpg.service.mapper.ProyectoGradoMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class SeguimientoService {

    private final ProyectoGradoRepository proyectos;
    private final ProyectoAccessService acceso;
    private final EntregaRepository entregas;
    private final ObservacionRepository observaciones;
    private final EvaluacionRepository evaluaciones;
    private final FechaLimiteEtapaRepository fechas;
    private final HistorialEtapaRepository historial;
    private final ProyectoGradoMapper proyectoMapper;
    private final EvaluacionMapper evaluacionMapper;
    private final HistorialEtapaMapper historialMapper;
    private final EvaluadorRequisitos evaluador;
    private final Clock clock;

    public SeguimientoService(
        ProyectoGradoRepository proyectos,
        ProyectoAccessService acceso,
        EntregaRepository entregas,
        ObservacionRepository observaciones,
        EvaluacionRepository evaluaciones,
        FechaLimiteEtapaRepository fechas,
        HistorialEtapaRepository historial,
        ProyectoGradoMapper proyectoMapper,
        EvaluacionMapper evaluacionMapper,
        HistorialEtapaMapper historialMapper,
        EvaluadorRequisitos evaluador,
        Clock clock
    ) {
        this.proyectos = proyectos;
        this.acceso = acceso;
        this.entregas = entregas;
        this.observaciones = observaciones;
        this.evaluaciones = evaluaciones;
        this.fechas = fechas;
        this.historial = historial;
        this.proyectoMapper = proyectoMapper;
        this.evaluacionMapper = evaluacionMapper;
        this.historialMapper = historialMapper;
        this.evaluador = evaluador;
        this.clock = clock;
    }

    public SeguimientoProyectoDTO obtener(Long proyectoId) {
        ProyectoGrado proyecto = proyectos
            .findById(proyectoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El proyecto no existe"));
        acceso.verificarPuedeVer(proyectoId);
        List<Entrega> documentos = entregas.findByProyectoIdOrderByEtapaAscVersionAsc(proyectoId);
        List<Evaluacion> notas = evaluaciones.findByProyectoIdOrderByFechaEvaluacionAsc(proyectoId);
        List<FechaLimiteEtapa> limites = fechas.findByProyectoIdOrderByFechaLimiteAsc(proyectoId);
        Instant ahora = Instant.now(clock);
        List<EtapaSeguimientoDTO> etapas = Arrays.stream(Etapa.values())
            .map(etapa -> {
                Instant limite = limites
                    .stream()
                    .filter(f -> f.getEtapa() == etapa)
                    .map(FechaLimiteEtapa::getFechaLimite)
                    .max(Instant::compareTo)
                    .orElse(null);
                Long diasRestantes =
                    limite == null
                        ? null
                        : ChronoUnit.DAYS.between(
                              ahora.atZone(clock.getZone()).toLocalDate(),
                              limite.atZone(clock.getZone()).toLocalDate()
                          );
                return new EtapaSeguimientoDTO(
                    etapa,
                    estadoEtapa(etapa, proyecto.getEtapaActual()),
                    limite,
                    limite != null && ahora.isAfter(limite),
                    diasRestantes,
                    documentos
                        .stream()
                        .filter(e -> e.getEtapa() == etapa)
                        .map(e ->
                            new EntregaResumenDTO(
                                e.getId(),
                                e.getTitulo(),
                                e.getVersion(),
                                e.getEstado(),
                                e.getFechaEntrega(),
                                observaciones.countByEntregaIdAndResueltaFalse(e.getId())
                            )
                        )
                        .toList(),
                    notas
                        .stream()
                        .filter(e -> e.getEtapa() == etapa)
                        .map(evaluacionMapper::toDto)
                        .toList()
                );
            })
            .toList();
        return new SeguimientoProyectoDTO(
            proyectoMapper.toDto(proyecto),
            proyecto.getPorcentajeAvance(),
            etapas,
            historial.findByProyectoIdOrderByFechaAsc(proyectoId).stream().map(historialMapper::toDto).toList(),
            evaluador.evaluar(proyecto)
        );
    }

    private String estadoEtapa(Etapa etapa, Etapa actual) {
        if (etapa.ordinal() < actual.ordinal() || actual == Etapa.FINALIZADO) {
            return "COMPLETADA";
        }
        return etapa == actual ? "ACTUAL" : "PENDIENTE";
    }
}
