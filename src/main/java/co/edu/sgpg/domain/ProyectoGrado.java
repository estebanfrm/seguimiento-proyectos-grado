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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    // Texto plano en PostgreSQL (no large object/OID): ver docs/decisiones.md
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
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
    @JsonIgnoreProperties(value = { "observacions", "autor", "proyecto" }, allowSetters = true)
    private Set<Entrega> entregas = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "proyecto")
    @JsonIgnoreProperties(value = { "proyecto" }, allowSetters = true)
    private Set<FechaLimiteEtapa> fechaLimites = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "proyecto")
    @JsonIgnoreProperties(value = { "jurado", "proyecto" }, allowSetters = true)
    private Set<Evaluacion> evaluacions = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "proyecto")
    @JsonIgnoreProperties(value = { "usuario", "proyecto" }, allowSetters = true)
    private Set<HistorialEtapa> historials = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private User tutor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_proyecto_grado__estudiante",
        joinColumns = @JoinColumn(name = "proyecto_grado_id"),
        inverseJoinColumns = @JoinColumn(name = "estudiante_id")
    )
    private Set<User> estudiantes = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_proyecto_grado__jurado",
        joinColumns = @JoinColumn(name = "proyecto_grado_id"),
        inverseJoinColumns = @JoinColumn(name = "jurado_id")
    )
    private Set<User> jurados = new HashSet<>();

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

    public Set<Entrega> getEntregas() {
        return this.entregas;
    }

    public void setEntregas(Set<Entrega> entregas) {
        if (this.entregas != null) {
            this.entregas.forEach(i -> i.setProyecto(null));
        }
        if (entregas != null) {
            entregas.forEach(i -> i.setProyecto(this));
        }
        this.entregas = entregas;
    }

    public ProyectoGrado entregas(Set<Entrega> entregas) {
        this.setEntregas(entregas);
        return this;
    }

    public ProyectoGrado addEntrega(Entrega entrega) {
        this.entregas.add(entrega);
        entrega.setProyecto(this);
        return this;
    }

    public ProyectoGrado removeEntrega(Entrega entrega) {
        this.entregas.remove(entrega);
        entrega.setProyecto(null);
        return this;
    }

    public Set<FechaLimiteEtapa> getFechaLimites() {
        return this.fechaLimites;
    }

    public void setFechaLimites(Set<FechaLimiteEtapa> fechaLimiteEtapas) {
        if (this.fechaLimites != null) {
            this.fechaLimites.forEach(i -> i.setProyecto(null));
        }
        if (fechaLimiteEtapas != null) {
            fechaLimiteEtapas.forEach(i -> i.setProyecto(this));
        }
        this.fechaLimites = fechaLimiteEtapas;
    }

    public ProyectoGrado fechaLimites(Set<FechaLimiteEtapa> fechaLimiteEtapas) {
        this.setFechaLimites(fechaLimiteEtapas);
        return this;
    }

    public ProyectoGrado addFechaLimite(FechaLimiteEtapa fechaLimiteEtapa) {
        this.fechaLimites.add(fechaLimiteEtapa);
        fechaLimiteEtapa.setProyecto(this);
        return this;
    }

    public ProyectoGrado removeFechaLimite(FechaLimiteEtapa fechaLimiteEtapa) {
        this.fechaLimites.remove(fechaLimiteEtapa);
        fechaLimiteEtapa.setProyecto(null);
        return this;
    }

    public Set<Evaluacion> getEvaluacions() {
        return this.evaluacions;
    }

    public void setEvaluacions(Set<Evaluacion> evaluacions) {
        if (this.evaluacions != null) {
            this.evaluacions.forEach(i -> i.setProyecto(null));
        }
        if (evaluacions != null) {
            evaluacions.forEach(i -> i.setProyecto(this));
        }
        this.evaluacions = evaluacions;
    }

    public ProyectoGrado evaluacions(Set<Evaluacion> evaluacions) {
        this.setEvaluacions(evaluacions);
        return this;
    }

    public ProyectoGrado addEvaluacion(Evaluacion evaluacion) {
        this.evaluacions.add(evaluacion);
        evaluacion.setProyecto(this);
        return this;
    }

    public ProyectoGrado removeEvaluacion(Evaluacion evaluacion) {
        this.evaluacions.remove(evaluacion);
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

    public Set<User> getEstudiantes() {
        return this.estudiantes;
    }

    public void setEstudiantes(Set<User> users) {
        this.estudiantes = users;
    }

    public ProyectoGrado estudiantes(Set<User> users) {
        this.setEstudiantes(users);
        return this;
    }

    public ProyectoGrado addEstudiante(User user) {
        this.estudiantes.add(user);
        return this;
    }

    public ProyectoGrado removeEstudiante(User user) {
        this.estudiantes.remove(user);
        return this;
    }

    public Set<User> getJurados() {
        return this.jurados;
    }

    public void setJurados(Set<User> users) {
        this.jurados = users;
    }

    public ProyectoGrado jurados(Set<User> users) {
        this.setJurados(users);
        return this;
    }

    public ProyectoGrado addJurado(User user) {
        this.jurados.add(user);
        return this;
    }

    public ProyectoGrado removeJurado(User user) {
        this.jurados.remove(user);
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
