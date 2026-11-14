package co.edu.sgpg.domain;

import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.domain.enumeration.Etapa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Bitácora de transiciones de etapa.
 */
@Entity
@Table(name = "historial_etapa")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HistorialEtapa implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_origen")
    private Etapa etapaOrigen;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_destino", nullable = false)
    private Etapa etapaDestino;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Size(max = 500)
    @Column(name = "comentario", length = 500)
    private String comentario;

    @ManyToOne(fetch = FetchType.LAZY)
    private User usuario;

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

    public HistorialEtapa id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Etapa getEtapaOrigen() {
        return this.etapaOrigen;
    }

    public HistorialEtapa etapaOrigen(Etapa etapaOrigen) {
        this.setEtapaOrigen(etapaOrigen);
        return this;
    }

    public void setEtapaOrigen(Etapa etapaOrigen) {
        this.etapaOrigen = etapaOrigen;
    }

    public Etapa getEtapaDestino() {
        return this.etapaDestino;
    }

    public HistorialEtapa etapaDestino(Etapa etapaDestino) {
        this.setEtapaDestino(etapaDestino);
        return this;
    }

    public void setEtapaDestino(Etapa etapaDestino) {
        this.etapaDestino = etapaDestino;
    }

    public Instant getFecha() {
        return this.fecha;
    }

    public HistorialEtapa fecha(Instant fecha) {
        this.setFecha(fecha);
        return this;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getComentario() {
        return this.comentario;
    }

    public HistorialEtapa comentario(String comentario) {
        this.setComentario(comentario);
        return this;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public User getUsuario() {
        return this.usuario;
    }

    public void setUsuario(User user) {
        this.usuario = user;
    }

    public HistorialEtapa usuario(User user) {
        this.setUsuario(user);
        return this;
    }

    public ProyectoGrado getProyecto() {
        return this.proyecto;
    }

    public void setProyecto(ProyectoGrado proyectoGrado) {
        this.proyecto = proyectoGrado;
    }

    public HistorialEtapa proyecto(ProyectoGrado proyectoGrado) {
        this.setProyecto(proyectoGrado);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistorialEtapa)) {
            return false;
        }
        return getId() != null && getId().equals(((HistorialEtapa) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HistorialEtapa{" +
            "id=" + getId() +
            ", etapaOrigen='" + getEtapaOrigen() + "'" +
            ", etapaDestino='" + getEtapaDestino() + "'" +
            ", fecha='" + getFecha() + "'" +
            ", comentario='" + getComentario() + "'" +
            "}";
    }
}
