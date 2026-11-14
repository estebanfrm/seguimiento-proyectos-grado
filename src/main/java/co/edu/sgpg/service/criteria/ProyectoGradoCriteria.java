package co.edu.sgpg.service.criteria;

import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link co.edu.sgpg.domain.ProyectoGrado} entity. This class is used
 * in {@link co.edu.sgpg.web.rest.ProyectoGradoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /proyecto-grados?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProyectoGradoCriteria implements Serializable, Criteria {

    /**
     * Class for filtering Etapa
     */
    public static class EtapaFilter extends Filter<Etapa> {

        public EtapaFilter() {}

        public EtapaFilter(EtapaFilter filter) {
            super(filter);
        }

        @Override
        public EtapaFilter copy() {
            return new EtapaFilter(this);
        }
    }

    /**
     * Class for filtering EstadoProyecto
     */
    public static class EstadoProyectoFilter extends Filter<EstadoProyecto> {

        public EstadoProyectoFilter() {}

        public EstadoProyectoFilter(EstadoProyectoFilter filter) {
            super(filter);
        }

        @Override
        public EstadoProyectoFilter copy() {
            return new EstadoProyectoFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter titulo;

    private StringFilter programa;

    private EtapaFilter etapaActual;

    private EstadoProyectoFilter estado;

    private IntegerFilter porcentajeAvance;

    private InstantFilter fechaRegistro;

    private InstantFilter fechaActualizacion;

    private LongFilter entregasId;

    private LongFilter fechasLimiteId;

    private LongFilter evaluacionesId;

    private LongFilter historialId;

    private LongFilter tutorId;

    private LongFilter estudiantesId;

    private LongFilter juradosId;

    private Boolean distinct;

    public ProyectoGradoCriteria() {}

    public ProyectoGradoCriteria(ProyectoGradoCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.titulo = other.optionalTitulo().map(StringFilter::copy).orElse(null);
        this.programa = other.optionalPrograma().map(StringFilter::copy).orElse(null);
        this.etapaActual = other.optionalEtapaActual().map(EtapaFilter::copy).orElse(null);
        this.estado = other.optionalEstado().map(EstadoProyectoFilter::copy).orElse(null);
        this.porcentajeAvance = other.optionalPorcentajeAvance().map(IntegerFilter::copy).orElse(null);
        this.fechaRegistro = other.optionalFechaRegistro().map(InstantFilter::copy).orElse(null);
        this.fechaActualizacion = other.optionalFechaActualizacion().map(InstantFilter::copy).orElse(null);
        this.entregasId = other.optionalEntregasId().map(LongFilter::copy).orElse(null);
        this.fechasLimiteId = other.optionalFechasLimiteId().map(LongFilter::copy).orElse(null);
        this.evaluacionesId = other.optionalEvaluacionesId().map(LongFilter::copy).orElse(null);
        this.historialId = other.optionalHistorialId().map(LongFilter::copy).orElse(null);
        this.tutorId = other.optionalTutorId().map(LongFilter::copy).orElse(null);
        this.estudiantesId = other.optionalEstudiantesId().map(LongFilter::copy).orElse(null);
        this.juradosId = other.optionalJuradosId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ProyectoGradoCriteria copy() {
        return new ProyectoGradoCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getTitulo() {
        return titulo;
    }

    public Optional<StringFilter> optionalTitulo() {
        return Optional.ofNullable(titulo);
    }

    public StringFilter titulo() {
        if (titulo == null) {
            setTitulo(new StringFilter());
        }
        return titulo;
    }

    public void setTitulo(StringFilter titulo) {
        this.titulo = titulo;
    }

    public StringFilter getPrograma() {
        return programa;
    }

    public Optional<StringFilter> optionalPrograma() {
        return Optional.ofNullable(programa);
    }

    public StringFilter programa() {
        if (programa == null) {
            setPrograma(new StringFilter());
        }
        return programa;
    }

    public void setPrograma(StringFilter programa) {
        this.programa = programa;
    }

    public EtapaFilter getEtapaActual() {
        return etapaActual;
    }

    public Optional<EtapaFilter> optionalEtapaActual() {
        return Optional.ofNullable(etapaActual);
    }

    public EtapaFilter etapaActual() {
        if (etapaActual == null) {
            setEtapaActual(new EtapaFilter());
        }
        return etapaActual;
    }

    public void setEtapaActual(EtapaFilter etapaActual) {
        this.etapaActual = etapaActual;
    }

    public EstadoProyectoFilter getEstado() {
        return estado;
    }

    public Optional<EstadoProyectoFilter> optionalEstado() {
        return Optional.ofNullable(estado);
    }

    public EstadoProyectoFilter estado() {
        if (estado == null) {
            setEstado(new EstadoProyectoFilter());
        }
        return estado;
    }

    public void setEstado(EstadoProyectoFilter estado) {
        this.estado = estado;
    }

    public IntegerFilter getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public Optional<IntegerFilter> optionalPorcentajeAvance() {
        return Optional.ofNullable(porcentajeAvance);
    }

    public IntegerFilter porcentajeAvance() {
        if (porcentajeAvance == null) {
            setPorcentajeAvance(new IntegerFilter());
        }
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(IntegerFilter porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public InstantFilter getFechaRegistro() {
        return fechaRegistro;
    }

    public Optional<InstantFilter> optionalFechaRegistro() {
        return Optional.ofNullable(fechaRegistro);
    }

    public InstantFilter fechaRegistro() {
        if (fechaRegistro == null) {
            setFechaRegistro(new InstantFilter());
        }
        return fechaRegistro;
    }

    public void setFechaRegistro(InstantFilter fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public InstantFilter getFechaActualizacion() {
        return fechaActualizacion;
    }

    public Optional<InstantFilter> optionalFechaActualizacion() {
        return Optional.ofNullable(fechaActualizacion);
    }

    public InstantFilter fechaActualizacion() {
        if (fechaActualizacion == null) {
            setFechaActualizacion(new InstantFilter());
        }
        return fechaActualizacion;
    }

    public void setFechaActualizacion(InstantFilter fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LongFilter getEntregasId() {
        return entregasId;
    }

    public Optional<LongFilter> optionalEntregasId() {
        return Optional.ofNullable(entregasId);
    }

    public LongFilter entregasId() {
        if (entregasId == null) {
            setEntregasId(new LongFilter());
        }
        return entregasId;
    }

    public void setEntregasId(LongFilter entregasId) {
        this.entregasId = entregasId;
    }

    public LongFilter getFechasLimiteId() {
        return fechasLimiteId;
    }

    public Optional<LongFilter> optionalFechasLimiteId() {
        return Optional.ofNullable(fechasLimiteId);
    }

    public LongFilter fechasLimiteId() {
        if (fechasLimiteId == null) {
            setFechasLimiteId(new LongFilter());
        }
        return fechasLimiteId;
    }

    public void setFechasLimiteId(LongFilter fechasLimiteId) {
        this.fechasLimiteId = fechasLimiteId;
    }

    public LongFilter getEvaluacionesId() {
        return evaluacionesId;
    }

    public Optional<LongFilter> optionalEvaluacionesId() {
        return Optional.ofNullable(evaluacionesId);
    }

    public LongFilter evaluacionesId() {
        if (evaluacionesId == null) {
            setEvaluacionesId(new LongFilter());
        }
        return evaluacionesId;
    }

    public void setEvaluacionesId(LongFilter evaluacionesId) {
        this.evaluacionesId = evaluacionesId;
    }

    public LongFilter getHistorialId() {
        return historialId;
    }

    public Optional<LongFilter> optionalHistorialId() {
        return Optional.ofNullable(historialId);
    }

    public LongFilter historialId() {
        if (historialId == null) {
            setHistorialId(new LongFilter());
        }
        return historialId;
    }

    public void setHistorialId(LongFilter historialId) {
        this.historialId = historialId;
    }

    public LongFilter getTutorId() {
        return tutorId;
    }

    public Optional<LongFilter> optionalTutorId() {
        return Optional.ofNullable(tutorId);
    }

    public LongFilter tutorId() {
        if (tutorId == null) {
            setTutorId(new LongFilter());
        }
        return tutorId;
    }

    public void setTutorId(LongFilter tutorId) {
        this.tutorId = tutorId;
    }

    public LongFilter getEstudiantesId() {
        return estudiantesId;
    }

    public Optional<LongFilter> optionalEstudiantesId() {
        return Optional.ofNullable(estudiantesId);
    }

    public LongFilter estudiantesId() {
        if (estudiantesId == null) {
            setEstudiantesId(new LongFilter());
        }
        return estudiantesId;
    }

    public void setEstudiantesId(LongFilter estudiantesId) {
        this.estudiantesId = estudiantesId;
    }

    public LongFilter getJuradosId() {
        return juradosId;
    }

    public Optional<LongFilter> optionalJuradosId() {
        return Optional.ofNullable(juradosId);
    }

    public LongFilter juradosId() {
        if (juradosId == null) {
            setJuradosId(new LongFilter());
        }
        return juradosId;
    }

    public void setJuradosId(LongFilter juradosId) {
        this.juradosId = juradosId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ProyectoGradoCriteria that = (ProyectoGradoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(titulo, that.titulo) &&
            Objects.equals(programa, that.programa) &&
            Objects.equals(etapaActual, that.etapaActual) &&
            Objects.equals(estado, that.estado) &&
            Objects.equals(porcentajeAvance, that.porcentajeAvance) &&
            Objects.equals(fechaRegistro, that.fechaRegistro) &&
            Objects.equals(fechaActualizacion, that.fechaActualizacion) &&
            Objects.equals(entregasId, that.entregasId) &&
            Objects.equals(fechasLimiteId, that.fechasLimiteId) &&
            Objects.equals(evaluacionesId, that.evaluacionesId) &&
            Objects.equals(historialId, that.historialId) &&
            Objects.equals(tutorId, that.tutorId) &&
            Objects.equals(estudiantesId, that.estudiantesId) &&
            Objects.equals(juradosId, that.juradosId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            titulo,
            programa,
            etapaActual,
            estado,
            porcentajeAvance,
            fechaRegistro,
            fechaActualizacion,
            entregasId,
            fechasLimiteId,
            evaluacionesId,
            historialId,
            tutorId,
            estudiantesId,
            juradosId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProyectoGradoCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTitulo().map(f -> "titulo=" + f + ", ").orElse("") +
            optionalPrograma().map(f -> "programa=" + f + ", ").orElse("") +
            optionalEtapaActual().map(f -> "etapaActual=" + f + ", ").orElse("") +
            optionalEstado().map(f -> "estado=" + f + ", ").orElse("") +
            optionalPorcentajeAvance().map(f -> "porcentajeAvance=" + f + ", ").orElse("") +
            optionalFechaRegistro().map(f -> "fechaRegistro=" + f + ", ").orElse("") +
            optionalFechaActualizacion().map(f -> "fechaActualizacion=" + f + ", ").orElse("") +
            optionalEntregasId().map(f -> "entregasId=" + f + ", ").orElse("") +
            optionalFechasLimiteId().map(f -> "fechasLimiteId=" + f + ", ").orElse("") +
            optionalEvaluacionesId().map(f -> "evaluacionesId=" + f + ", ").orElse("") +
            optionalHistorialId().map(f -> "historialId=" + f + ", ").orElse("") +
            optionalTutorId().map(f -> "tutorId=" + f + ", ").orElse("") +
            optionalEstudiantesId().map(f -> "estudiantesId=" + f + ", ").orElse("") +
            optionalJuradosId().map(f -> "juradosId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
