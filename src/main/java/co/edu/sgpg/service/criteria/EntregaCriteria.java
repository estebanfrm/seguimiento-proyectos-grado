package co.edu.sgpg.service.criteria;

import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.Etapa;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link co.edu.sgpg.domain.Entrega} entity. This class is used
 * in {@link co.edu.sgpg.web.rest.EntregaResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /entregas?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EntregaCriteria implements Serializable, Criteria {

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
     * Class for filtering EstadoEntrega
     */
    public static class EstadoEntregaFilter extends Filter<EstadoEntrega> {

        public EstadoEntregaFilter() {}

        public EstadoEntregaFilter(EstadoEntregaFilter filter) {
            super(filter);
        }

        @Override
        public EstadoEntregaFilter copy() {
            return new EstadoEntregaFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private EtapaFilter etapa;

    private StringFilter titulo;

    private StringFilter urlDocumento;

    private IntegerFilter version;

    private InstantFilter fechaEntrega;

    private EstadoEntregaFilter estado;

    private LongFilter observacionesId;

    private LongFilter autorId;

    private LongFilter proyectoId;

    private Boolean distinct;

    public EntregaCriteria() {}

    public EntregaCriteria(EntregaCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.etapa = other.optionalEtapa().map(EtapaFilter::copy).orElse(null);
        this.titulo = other.optionalTitulo().map(StringFilter::copy).orElse(null);
        this.urlDocumento = other.optionalUrlDocumento().map(StringFilter::copy).orElse(null);
        this.version = other.optionalVersion().map(IntegerFilter::copy).orElse(null);
        this.fechaEntrega = other.optionalFechaEntrega().map(InstantFilter::copy).orElse(null);
        this.estado = other.optionalEstado().map(EstadoEntregaFilter::copy).orElse(null);
        this.observacionesId = other.optionalObservacionesId().map(LongFilter::copy).orElse(null);
        this.autorId = other.optionalAutorId().map(LongFilter::copy).orElse(null);
        this.proyectoId = other.optionalProyectoId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public EntregaCriteria copy() {
        return new EntregaCriteria(this);
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

    public EtapaFilter getEtapa() {
        return etapa;
    }

    public Optional<EtapaFilter> optionalEtapa() {
        return Optional.ofNullable(etapa);
    }

    public EtapaFilter etapa() {
        if (etapa == null) {
            setEtapa(new EtapaFilter());
        }
        return etapa;
    }

    public void setEtapa(EtapaFilter etapa) {
        this.etapa = etapa;
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

    public StringFilter getUrlDocumento() {
        return urlDocumento;
    }

    public Optional<StringFilter> optionalUrlDocumento() {
        return Optional.ofNullable(urlDocumento);
    }

    public StringFilter urlDocumento() {
        if (urlDocumento == null) {
            setUrlDocumento(new StringFilter());
        }
        return urlDocumento;
    }

    public void setUrlDocumento(StringFilter urlDocumento) {
        this.urlDocumento = urlDocumento;
    }

    public IntegerFilter getVersion() {
        return version;
    }

    public Optional<IntegerFilter> optionalVersion() {
        return Optional.ofNullable(version);
    }

    public IntegerFilter version() {
        if (version == null) {
            setVersion(new IntegerFilter());
        }
        return version;
    }

    public void setVersion(IntegerFilter version) {
        this.version = version;
    }

    public InstantFilter getFechaEntrega() {
        return fechaEntrega;
    }

    public Optional<InstantFilter> optionalFechaEntrega() {
        return Optional.ofNullable(fechaEntrega);
    }

    public InstantFilter fechaEntrega() {
        if (fechaEntrega == null) {
            setFechaEntrega(new InstantFilter());
        }
        return fechaEntrega;
    }

    public void setFechaEntrega(InstantFilter fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public EstadoEntregaFilter getEstado() {
        return estado;
    }

    public Optional<EstadoEntregaFilter> optionalEstado() {
        return Optional.ofNullable(estado);
    }

    public EstadoEntregaFilter estado() {
        if (estado == null) {
            setEstado(new EstadoEntregaFilter());
        }
        return estado;
    }

    public void setEstado(EstadoEntregaFilter estado) {
        this.estado = estado;
    }

    public LongFilter getObservacionesId() {
        return observacionesId;
    }

    public Optional<LongFilter> optionalObservacionesId() {
        return Optional.ofNullable(observacionesId);
    }

    public LongFilter observacionesId() {
        if (observacionesId == null) {
            setObservacionesId(new LongFilter());
        }
        return observacionesId;
    }

    public void setObservacionesId(LongFilter observacionesId) {
        this.observacionesId = observacionesId;
    }

    public LongFilter getAutorId() {
        return autorId;
    }

    public Optional<LongFilter> optionalAutorId() {
        return Optional.ofNullable(autorId);
    }

    public LongFilter autorId() {
        if (autorId == null) {
            setAutorId(new LongFilter());
        }
        return autorId;
    }

    public void setAutorId(LongFilter autorId) {
        this.autorId = autorId;
    }

    public LongFilter getProyectoId() {
        return proyectoId;
    }

    public Optional<LongFilter> optionalProyectoId() {
        return Optional.ofNullable(proyectoId);
    }

    public LongFilter proyectoId() {
        if (proyectoId == null) {
            setProyectoId(new LongFilter());
        }
        return proyectoId;
    }

    public void setProyectoId(LongFilter proyectoId) {
        this.proyectoId = proyectoId;
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
        final EntregaCriteria that = (EntregaCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(etapa, that.etapa) &&
            Objects.equals(titulo, that.titulo) &&
            Objects.equals(urlDocumento, that.urlDocumento) &&
            Objects.equals(version, that.version) &&
            Objects.equals(fechaEntrega, that.fechaEntrega) &&
            Objects.equals(estado, that.estado) &&
            Objects.equals(observacionesId, that.observacionesId) &&
            Objects.equals(autorId, that.autorId) &&
            Objects.equals(proyectoId, that.proyectoId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, etapa, titulo, urlDocumento, version, fechaEntrega, estado, observacionesId, autorId, proyectoId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EntregaCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalEtapa().map(f -> "etapa=" + f + ", ").orElse("") +
            optionalTitulo().map(f -> "titulo=" + f + ", ").orElse("") +
            optionalUrlDocumento().map(f -> "urlDocumento=" + f + ", ").orElse("") +
            optionalVersion().map(f -> "version=" + f + ", ").orElse("") +
            optionalFechaEntrega().map(f -> "fechaEntrega=" + f + ", ").orElse("") +
            optionalEstado().map(f -> "estado=" + f + ", ").orElse("") +
            optionalObservacionesId().map(f -> "observacionesId=" + f + ", ").orElse("") +
            optionalAutorId().map(f -> "autorId=" + f + ", ").orElse("") +
            optionalProyectoId().map(f -> "proyectoId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
