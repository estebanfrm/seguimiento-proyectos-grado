package co.edu.sgpg.domain;

import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Proyecto de grado: unidad central del seguimiento.
 */
@Entity
@Table(name = "proyecto_grado")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProyectoGrado implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 10, max = 250)
    @Column(name = "titulo", length = 250, nullable = false)
    private String titulo;

    @Lob
    @Column(name = "resumen", nullable = false)
    private String resumen;

    @NotNull
    @Size(max = 120)
    @Column(name = "programa", length = 120, nullable = false)
    private String programa;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_actual", nullable = false)
    private Etapa etapaActual;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoProyecto estado;

    @Min(value = 0)
    @Max(value = 100)
    @Column(name = "porcentaje_avance")
    private Integer porcentajeAvance;

    @NotNull
    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "proyecto")
    @JsonIgnoreProperties(value = { "observacioneses", "autor", "proyecto" }, allowSetters = true)
    private Set<Entrega> entregases = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "proyecto")
    @JsonIgnoreProperties(value = { "proyecto" }, allowSetters = true)
    private Set<FechaLimiteEtapa> fechasLimites = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "proyecto")
    @JsonIgnoreProperties(value = { "jurado", "proyecto" }, allowSetters = true)
    private Set<Evaluacion> evaluacioneses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "proyecto")
    @JsonIgnoreProperties(value = { "usuario", "proyecto" }, allowSetters = true)
    private Set<HistorialEtapa> historials = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private User tutor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_proyecto_grado__estudiantes",
        joinColumns = @JoinColumn(name = "proyecto_grado_id"),
        inverseJoinColumns = @JoinColumn(name = "estudiantes_id")
    )
    private Set<User> estudianteses = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_proyecto_grado__jurados",
        joinColumns = @JoinColumn(name = "proyecto_grado_id"),
        inverseJoinColumns = @JoinColumn(name = "jurados_id")
    )
    private Set<User> juradoses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ProyectoGrado id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public ProyectoGrado titulo(String titulo) {
        this.setTitulo(titulo);
        return this;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getResumen() {
        return this.resumen;
    }

    public ProyectoGrado resumen(String resumen) {
        this.setResumen(resumen);
        return this;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public String getPrograma() {
        return this.programa;
    }

    public ProyectoGrado programa(String programa) {
        this.setPrograma(programa);
        return this;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public Etapa getEtapaActual() {
        return this.etapaActual;
    }

    public ProyectoGrado etapaActual(Etapa etapaActual) {
        this.setEtapaActual(etapaActual);
        return this;
    }

    public void setEtapaActual(Etapa etapaActual) {
        this.etapaActual = etapaActual;
    }

    public EstadoProyecto getEstado() {
        return this.estado;
    }

    public ProyectoGrado estado(EstadoProyecto estado) {
        this.setEstado(estado);
        return this;
    }

    public void setEstado(EstadoProyecto estado) {
        this.estado = estado;
    }

    public Integer getPorcentajeAvance() {
        return this.porcentajeAvance;
    }

    public ProyectoGrado porcentajeAvance(Integer porcentajeAvance) {
        this.setPorcentajeAvance(porcentajeAvance);
        return this;
    }

    public void setPorcentajeAvance(Integer porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public Instant getFechaRegistro() {
        return this.fechaRegistro;
    }

    public ProyectoGrado fechaRegistro(Instant fechaRegistro) {
        this.setFechaRegistro(fechaRegistro);
        return this;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Instant getFechaActualizacion() {
        return this.fechaActualizacion;
    }

    public ProyectoGrado fechaActualizacion(Instant fechaActualizacion) {
        this.setFechaActualizacion(fechaActualizacion);
        return this;
    }

    public void setFechaActualizacion(Instant fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Set<Entrega> getEntregases() {
        return this.entregases;
    }

    public void setEntregases(Set<Entrega> entregas) {
        if (this.entregases != null) {
            this.entregases.forEach(i -> i.setProyecto(null));
        }
        if (entregas != null) {
            entregas.forEach(i -> i.setProyecto(this));
        }
        this.entregases = entregas;
    }

    public ProyectoGrado entregases(Set<Entrega> entregas) {
        this.setEntregases(entregas);
        return this;
    }

    public ProyectoGrado addEntregas(Entrega entrega) {
        this.entregases.add(entrega);
        entrega.setProyecto(this);
        return this;
    }

    public ProyectoGrado removeEntregas(Entrega entrega) {
        this.entregases.remove(entrega);
        entrega.setProyecto(null);
        return this;
    }

    public Set<FechaLimiteEtapa> getFechasLimites() {
        return this.fechasLimites;
    }

    public void setFechasLimites(Set<FechaLimiteEtapa> fechaLimiteEtapas) {
        if (this.fechasLimites != null) {
            this.fechasLimites.forEach(i -> i.setProyecto(null));
        }
        if (fechaLimiteEtapas != null) {
            fechaLimiteEtapas.forEach(i -> i.setProyecto(this));
        }
        this.fechasLimites = fechaLimiteEtapas;
    }

    public ProyectoGrado fechasLimites(Set<FechaLimiteEtapa> fechaLimiteEtapas) {
        this.setFechasLimites(fechaLimiteEtapas);
        return this;
    }

    public ProyectoGrado addFechasLimite(FechaLimiteEtapa fechaLimiteEtapa) {
        this.fechasLimites.add(fechaLimiteEtapa);
        fechaLimiteEtapa.setProyecto(this);
        return this;
    }

    public ProyectoGrado removeFechasLimite(FechaLimiteEtapa fechaLimiteEtapa) {
        this.fechasLimites.remove(fechaLimiteEtapa);
        fechaLimiteEtapa.setProyecto(null);
        return this;
    }

    public Set<Evaluacion> getEvaluacioneses() {
        return this.evaluacioneses;
    }

    public void setEvaluacioneses(Set<Evaluacion> evaluacions) {
        if (this.evaluacioneses != null) {
            this.evaluacioneses.forEach(i -> i.setProyecto(null));
        }
        if (evaluacions != null) {
            evaluacions.forEach(i -> i.setProyecto(this));
        }
        this.evaluacioneses = evaluacions;
    }

    public ProyectoGrado evaluacioneses(Set<Evaluacion> evaluacions) {
        this.setEvaluacioneses(evaluacions);
        return this;
    }

    public ProyectoGrado addEvaluaciones(Evaluacion evaluacion) {
        this.evaluacioneses.add(evaluacion);
        evaluacion.setProyecto(this);
        return this;
    }

    public ProyectoGrado removeEvaluaciones(Evaluacion evaluacion) {
        this.evaluacioneses.remove(evaluacion);
        evaluacion.setProyecto(null);
        return this;
    }

    public Set<HistorialEtapa> getHistorials() {
        return this.historials;
    }

    public void setHistorials(Set<HistorialEtapa> historialEtapas) {
        if (this.historials != null) {
            this.historials.forEach(i -> i.setProyecto(null));
        }
        if (historialEtapas != null) {
            historialEtapas.forEach(i -> i.setProyecto(this));
        }
        this.historials = historialEtapas;
    }

    public ProyectoGrado historials(Set<HistorialEtapa> historialEtapas) {
        this.setHistorials(historialEtapas);
        return this;
    }

    public ProyectoGrado addHistorial(HistorialEtapa historialEtapa) {
        this.historials.add(historialEtapa);
        historialEtapa.setProyecto(this);
        return this;
    }

    public ProyectoGrado removeHistorial(HistorialEtapa historialEtapa) {
        this.historials.remove(historialEtapa);
        historialEtapa.setProyecto(null);
        return this;
    }

    public User getTutor() {
        return this.tutor;
    }

    public void setTutor(User user) {
        this.tutor = user;
    }

    public ProyectoGrado tutor(User user) {
        this.setTutor(user);
        return this;
    }

    public Set<User> getEstudianteses() {
        return this.estudianteses;
    }

    public void setEstudianteses(Set<User> users) {
        this.estudianteses = users;
    }

    public ProyectoGrado estudianteses(Set<User> users) {
        this.setEstudianteses(users);
        return this;
    }

    public ProyectoGrado addEstudiantes(User user) {
        this.estudianteses.add(user);
        return this;
    }

    public ProyectoGrado removeEstudiantes(User user) {
        this.estudianteses.remove(user);
        return this;
    }

    public Set<User> getJuradoses() {
        return this.juradoses;
    }

    public void setJuradoses(Set<User> users) {
        this.juradoses = users;
    }

    public ProyectoGrado juradoses(Set<User> users) {
        this.setJuradoses(users);
        return this;
    }

    public ProyectoGrado addJurados(User user) {
        this.juradoses.add(user);
        return this;
    }

    public ProyectoGrado removeJurados(User user) {
        this.juradoses.remove(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProyectoGrado)) {
            return false;
        }
        return getId() != null && getId().equals(((ProyectoGrado) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProyectoGrado{" +
            "id=" + getId() +
            ", titulo='" + getTitulo() + "'" +
            ", resumen='" + getResumen() + "'" +
            ", programa='" + getPrograma() + "'" +
            ", etapaActual='" + getEtapaActual() + "'" +
            ", estado='" + getEstado() + "'" +
            ", porcentajeAvance=" + getPorcentajeAvance() +
            ", fechaRegistro='" + getFechaRegistro() + "'" +
            ", fechaActualizacion='" + getFechaActualizacion() + "'" +
            "}";
    }
}
