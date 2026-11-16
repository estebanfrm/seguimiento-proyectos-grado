package co.edu.sgpg.domain;

import co.edu.sgpg.domain.enumeration.TipoObservacion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Observación del tutor sobre una entrega.
 */
@Entity
@Table(name = "observacion")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Observacion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    // Texto plano en PostgreSQL (no large object/OID): ver docs/decisiones.md
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "contenido", nullable = false)
    private String contenido;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoObservacion tipo;

    @NotNull
    @Column(name = "resuelta", nullable = false)
    private Boolean resuelta;

    @NotNull
    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    private User autor;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "observacions", "autor", "proyecto" }, allowSetters = true)
    private Entrega entrega;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Observacion id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenido() {
        return this.contenido;
    }

    public Observacion contenido(String contenido) {
        this.setContenido(contenido);
        return this;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public TipoObservacion getTipo() {
        return this.tipo;
    }

    public Observacion tipo(TipoObservacion tipo) {
        this.setTipo(tipo);
        return this;
    }

    public void setTipo(TipoObservacion tipo) {
        this.tipo = tipo;
    }

    public Boolean getResuelta() {
        return this.resuelta;
    }

    public Observacion resuelta(Boolean resuelta) {
        this.setResuelta(resuelta);
        return this;
    }

    public void setResuelta(Boolean resuelta) {
        this.resuelta = resuelta;
    }

    public Instant getFechaCreacion() {
        return this.fechaCreacion;
    }

    public Observacion fechaCreacion(Instant fechaCreacion) {
        this.setFechaCreacion(fechaCreacion);
        return this;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public User getAutor() {
        return this.autor;
    }

    public void setAutor(User user) {
        this.autor = user;
    }

    public Observacion autor(User user) {
        this.setAutor(user);
        return this;
    }

    public Entrega getEntrega() {
        return this.entrega;
    }

    public void setEntrega(Entrega entrega) {
        this.entrega = entrega;
    }

    public Observacion entrega(Entrega entrega) {
        this.setEntrega(entrega);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Observacion)) {
            return false;
        }
        return getId() != null && getId().equals(((Observacion) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Observacion{" +
            "id=" + getId() +
            ", contenido='" + getContenido() + "'" +
            ", tipo='" + getTipo() + "'" +
            ", resuelta='" + getResuelta() + "'" +
            ", fechaCreacion='" + getFechaCreacion() + "'" +
            "}";
    }
}
