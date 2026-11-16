package co.edu.sgpg.domain;

import co.edu.sgpg.domain.enumeration.Concepto;
import co.edu.sgpg.domain.enumeration.Etapa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Evaluación de un jurado en una etapa evaluable.
 */
@Entity
@Table(name = "evaluacion")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Evaluacion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "etapa", nullable = false)
    private Etapa etapa;

    @NotNull
    @DecimalMin(value = "0")
    @DecimalMax(value = "5")
    @Column(name = "calificacion", precision = 21, scale = 2, nullable = false)
    private BigDecimal calificacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "concepto", nullable = false)
    private Concepto concepto;

    // Texto plano en PostgreSQL (no large object/OID): ver docs/decisiones.md
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "comentarios")
    private String comentarios;

    @NotNull
    @Column(name = "fecha_evaluacion", nullable = false)
    private Instant fechaEvaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    private User jurado;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "entregas", "fechaLimites", "evaluacions", "historials", "tutor", "estudiantes", "jurados" },
        allowSetters = true
    )
    private ProyectoGrado proyecto;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Evaluacion id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Etapa getEtapa() {
        return this.etapa;
    }

    public Evaluacion etapa(Etapa etapa) {
        this.setEtapa(etapa);
        return this;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }

    public BigDecimal getCalificacion() {
        return this.calificacion;
    }

    public Evaluacion calificacion(BigDecimal calificacion) {
        this.setCalificacion(calificacion);
        return this;
    }

    public void setCalificacion(BigDecimal calificacion) {
        this.calificacion = calificacion;
    }

    public Concepto getConcepto() {
        return this.concepto;
    }

    public Evaluacion concepto(Concepto concepto) {
        this.setConcepto(concepto);
        return this;
    }

    public void setConcepto(Concepto concepto) {
        this.concepto = concepto;
    }

    public String getComentarios() {
        return this.comentarios;
    }

    public Evaluacion comentarios(String comentarios) {
        this.setComentarios(comentarios);
        return this;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public Instant getFechaEvaluacion() {
        return this.fechaEvaluacion;
    }

    public Evaluacion fechaEvaluacion(Instant fechaEvaluacion) {
        this.setFechaEvaluacion(fechaEvaluacion);
        return this;
    }

    public void setFechaEvaluacion(Instant fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public User getJurado() {
        return this.jurado;
    }

    public void setJurado(User user) {
        this.jurado = user;
    }

    public Evaluacion jurado(User user) {
        this.setJurado(user);
        return this;
    }

    public ProyectoGrado getProyecto() {
        return this.proyecto;
    }

    public void setProyecto(ProyectoGrado proyectoGrado) {
        this.proyecto = proyectoGrado;
    }

    public Evaluacion proyecto(ProyectoGrado proyectoGrado) {
        this.setProyecto(proyectoGrado);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Evaluacion)) {
            return false;
        }
        return getId() != null && getId().equals(((Evaluacion) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Evaluacion{" +
            "id=" + getId() +
            ", etapa='" + getEtapa() + "'" +
            ", calificacion=" + getCalificacion() +
            ", concepto='" + getConcepto() + "'" +
            ", comentarios='" + getComentarios() + "'" +
            ", fechaEvaluacion='" + getFechaEvaluacion() + "'" +
            "}";
    }
}
