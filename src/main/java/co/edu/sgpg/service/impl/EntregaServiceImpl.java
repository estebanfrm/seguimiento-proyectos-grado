package co.edu.sgpg.service.impl;

import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.*;
import co.edu.sgpg.service.*;
import co.edu.sgpg.service.dto.ArchivoEntregaDTO;
import co.edu.sgpg.service.dto.EntregaDTO;
import co.edu.sgpg.service.dto.RevisionEntregaDTO;
import co.edu.sgpg.service.mapper.EntregaMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository repositorio;
    private final EntregaMapper mapper;
    private final FechaLimiteEtapaRepository fechas;
    private final ObservacionRepository observaciones;
    private final ProyectoAccessService acceso;
    private final PermisosEntidadService permisos;
    private final VisibilidadProyectoService visibilidad;
    private final AvanceService avance;
    private final Clock clock;
    private final long tamanoMaximoArchivo;

    public EntregaServiceImpl(
        EntregaRepository repositorio,
        EntregaMapper mapper,
        FechaLimiteEtapaRepository fechas,
        ObservacionRepository observaciones,
        ProyectoAccessService acceso,
        PermisosEntidadService permisos,
        VisibilidadProyectoService visibilidad,
        AvanceService avance,
        Clock clock,
        @Value("${sgpg.entregas.tamano-maximo-archivo:10MB}") DataSize tamanoMaximoArchivo
    ) {
        this.repositorio = repositorio;
        this.mapper = mapper;
        this.fechas = fechas;
        this.observaciones = observaciones;
        this.acceso = acceso;
        this.permisos = permisos;
        this.visibilidad = visibilidad;
        this.avance = avance;
        this.clock = clock;
        this.tamanoMaximoArchivo = tamanoMaximoArchivo.toBytes();
    }

    @Override
    public EntregaDTO save(EntregaDTO dto) {
        Long proyectoId = dto.getProyecto() == null ? null : dto.getProyecto().getId();
        permisos.estudiante(proyectoId);
        ProyectoGrado proyecto = permisos.proyecto(proyectoId);
        Instant ahora = Instant.now(clock);
        if (proyecto.getEstado() != EstadoProyecto.ACTIVO || proyecto.getEtapaActual() == Etapa.FINALIZADO) {
            throw new ReglaNegocioException("PROYECTO_NO_ACTIVO", "El proyecto no admite entregas");
        }
        Etapa etapa = proyecto.getEtapaActual();
        fechas.findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(proyectoId, etapa).ifPresent(fecha -> {
            if (ahora.isAfter(fecha.getFechaLimite())) throw new ReglaNegocioException(
                "FECHA_LIMITE_VENCIDA",
                "La fecha límite de la etapa ha vencido"
            );
        });
        if (
            repositorio.existsByProyectoIdAndEtapaAndEstadoIn(proyectoId, etapa, List.of(EstadoEntrega.ENVIADA, EstadoEntrega.EN_REVISION))
        ) {
            throw new ReglaNegocioException("ENTREGA_EN_CURSO", "Ya existe una entrega en curso en esta etapa");
        }
        if (repositorio.existsByProyectoIdAndEtapaAndEstado(proyectoId, etapa, EstadoEntrega.APROBADA)) {
            throw new ReglaNegocioException("ETAPA_YA_APROBADA", "La etapa ya tiene una entrega aprobada");
        }
        validarArchivo(dto);
        if (dto.getArchivo() == null && (dto.getUrlDocumento() == null || dto.getUrlDocumento().isBlank())) {
            throw new ReglaNegocioException("DOCUMENTO_REQUERIDO", "Adjunte un PDF o indique la URL del documento");
        }
        Entrega entrega = mapper.toEntity(dto);
        if (entrega.getArchivo() == null) {
            entrega.setArchivoContentType(null);
            entrega.setArchivoNombre(null);
        }
        entrega.setProyecto(proyecto);
        entrega.setEtapa(etapa);
        entrega.setVersion(repositorio.findMaxVersion(proyectoId, etapa) + 1);
        entrega.setEstado(EstadoEntrega.ENVIADA);
        entrega.setAutor(permisos.usuarioActual());
        entrega.setFechaEntrega(ahora);
        entrega = repositorio.save(entrega);
        avance.recalcular(proyectoId);
        return mapper.toDto(entrega);
    }

    @Override
    public EntregaDTO update(EntregaDTO dto) {
        return modificar(dto, false).orElseThrow();
    }

    @Override
    public Optional<EntregaDTO> partialUpdate(EntregaDTO dto) {
        return modificar(dto, true);
    }

    private Optional<EntregaDTO> modificar(EntregaDTO dto, boolean parcial) {
        return repositorio.findById(dto.getId()).map(entrega -> {
            if (!acceso.esAdmin()) {
                permisos.estudiante(entrega.getProyecto().getId());
                if (entrega.getEstado() != EstadoEntrega.ENVIADA) throw new AccessDeniedException(
                    "Solo se editan entregas en estado ENVIADA"
                );
            }
            validarArchivo(dto);
            if (!parcial || dto.getTitulo() != null) entrega.setTitulo(dto.getTitulo());
            if (!parcial || dto.getDescripcion() != null) entrega.setDescripcion(dto.getDescripcion());
            // Los bytes son solo de escritura: omitirlos al editar conserva el PDF existente.
            if (dto.getArchivo() != null) {
                entrega.setArchivo(dto.getArchivo());
                entrega.setArchivoContentType(dto.getArchivoContentType());
                entrega.setArchivoNombre(dto.getArchivoNombre());
            }
            if (!parcial || dto.getUrlDocumento() != null) entrega.setUrlDocumento(dto.getUrlDocumento());
            return mapper.toDto(repositorio.save(entrega));
        });
    }

    private void validarArchivo(EntregaDTO dto) {
        byte[] archivo = dto.getArchivo();
        if (archivo == null) return;
        if (archivo.length > tamanoMaximoArchivo) {
            throw new ReglaNegocioException("ARCHIVO_DEMASIADO_GRANDE", "El PDF supera el tamaño máximo permitido");
        }
        byte[] firma = "%PDF-".getBytes(StandardCharsets.US_ASCII);
        if (!"application/pdf".equals(dto.getArchivoContentType()) || archivo.length < firma.length) {
            throw new ReglaNegocioException("ARCHIVO_NO_PDF", "El archivo debe ser un PDF válido");
        }
        for (int i = 0; i < firma.length; i++) {
            if (archivo[i] != firma[i]) throw new ReglaNegocioException("ARCHIVO_NO_PDF", "El archivo debe comenzar con %PDF-");
        }
        if (dto.getArchivoNombre() != null && dto.getArchivoNombre().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del archivo admite hasta 255 caracteres");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArchivoEntregaDTO> obtenerArchivo(Long id) {
        return repositorio.findById(id).flatMap(entrega -> {
            acceso.verificarPuedeVer(entrega.getProyecto().getId());
            if (entrega.getArchivo() == null || entrega.getArchivo().length == 0) return Optional.empty();
            String nombre = entrega.getArchivoNombre();
            return Optional.of(new ArchivoEntregaDTO(entrega.getArchivo(), nombre == null || nombre.isBlank() ? "entrega.pdf" : nombre));
        });
    }

    @Override
    public Optional<EntregaDTO> revisar(Long id, RevisionEntregaDTO revision) {
        return repositorio.findById(id).map(entrega -> {
            Long proyectoId = entrega.getProyecto().getId();
            permisos.tutorOAdmin(proyectoId);
            if (entrega.getEtapa() != entrega.getProyecto().getEtapaActual()) {
                throw new AccessDeniedException("Solo se revisan entregas de la etapa actual");
            }
            EstadoEntrega estado = revision.estado();
            if (estado == null || estado == EstadoEntrega.ENVIADA) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de revisión inválido");
            }
            if (
                estado == EstadoEntrega.APROBADA &&
                observaciones.countByEntregaIdAndTipoAndResueltaFalse(id, TipoObservacion.CORRECCION_REQUERIDA) > 0
            ) {
                throw new ReglaNegocioException("CORRECCIONES_PENDIENTES", "La entrega tiene correcciones pendientes");
            }
            entrega.setEstado(estado);
            entrega = repositorio.save(entrega);
            avance.recalcular(proyectoId);
            return mapper.toDto(entrega);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EntregaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return repositorio.findAll(visibilidad.para(root -> root.get("proyecto").get("id")), pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntregaDTO> findOne(Long id) {
        return repositorio.findOneWithEagerRelationships(id).map(entrega -> {
            acceso.verificarPuedeVer(entrega.getProyecto().getId());
            return mapper.toDto(entrega);
        });
    }

    @Override
    public void delete(Long id) {
        permisos.admin();
        repositorio.findById(id).ifPresent(entrega -> {
            Long proyectoId = entrega.getProyecto().getId();
            repositorio.delete(entrega);
            avance.recalcular(proyectoId);
        });
    }
}
