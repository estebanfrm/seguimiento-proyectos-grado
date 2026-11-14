package co.edu.sgpg.domain;

import co.edu.sgpg.domain.enumeration.Etapa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Fecha límite de una etapa para un proyecto.
 */
@Entity
@Table(name = "fecha_limite_etapa")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FechaLimiteEtapa implements Serializable {

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
    @Column(name = "fecha_limite", nullable = false)
    private Instant fechaLimite;

    @Size(max = 250)
    @Column(name = "descripcion", length = 250)
    private String descripcion;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "entregases", "fechasLimites", "evaluacioneses", "historials", "tutor", "estudianteses", "juradoses" },
        allowSetters = true
    )
    private ProyectoGrado proyecto;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public FechaLimiteEtapa id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Etapa getEtapa() {
        return this.etapa;
    }

    public FechaLimiteEtapa etapa(Etapa etapa) {
        this.setEtapa(etapa);
        return this;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }

    public Instant getFechaLimite() {
        return this.fechaLimite;
    }

    public FechaLimiteEtapa fechaLimite(Instant fechaLimite) {
        this.setFechaLimite(fechaLimite);
        return this;
    }

    public void setFechaLimite(Instant fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public FechaLimiteEtapa descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public ProyectoGrado getProyecto() {
        return this.proyecto;
    }

    public void setProyecto(ProyectoGrado proyectoGrado) {
        this.proyecto = proyectoGrado;
    }

    public FechaLimiteEtapa proyecto(ProyectoGrado proyectoGrado) {
        this.setProyecto(proyectoGrado);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FechaLimiteEtapa)) {
            return false;
        }
        return getId() != null && getId().equals(((FechaLimiteEtapa) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FechaLimiteEtapa{" +
            "id=" + getId() +
            ", etapa='" + getEtapa() + "'" +
            ", fechaLimite='" + getFechaLimite() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            "}";
    }
}
