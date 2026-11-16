package co.edu.sgpg.domain;

import co.edu.sgpg.domain.enumeration.EstadoEntrega;
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
 * Documento entregado por el estudiante en una etapa.
 */
@Entity
@Table(name = "entrega")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Entrega implements Serializable {

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
    @Size(max = 200)
    @Column(name = "titulo", length = 200, nullable = false)
    private String titulo;

    // Texto plano en PostgreSQL (no large object/OID): ver docs/decisiones.md
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "descripcion")
    private String descripcion;

    @Lob
    @Column(name = "archivo")
    private byte[] archivo;

    @Column(name = "archivo_content_type")
    private String archivoContentType;

    @Size(max = 500)
    @Column(name = "url_documento", length = 500)
    private String urlDocumento;

    @NotNull
    @Min(value = 1)
    @Column(name = "version", nullable = false)
    private Integer version;

    @NotNull
    @Column(name = "fecha_entrega", nullable = false)
    private Instant fechaEntrega;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEntrega estado;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "entrega")
    @JsonIgnoreProperties(value = { "autor", "entrega" }, allowSetters = true)
    private Set<Observacion> observacions = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private User autor;

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

    public Entrega id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Etapa getEtapa() {
        return this.etapa;
    }

    public Entrega etapa(Etapa etapa) {
        this.setEtapa(etapa);
        return this;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public Entrega titulo(String titulo) {
        this.setTitulo(titulo);
        return this;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public Entrega descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public byte[] getArchivo() {
        return this.archivo;
    }

    public Entrega archivo(byte[] archivo) {
        this.setArchivo(archivo);
        return this;
    }

    public void setArchivo(byte[] archivo) {
        this.archivo = archivo;
    }

    public String getArchivoContentType() {
        return this.archivoContentType;
    }

    public Entrega archivoContentType(String archivoContentType) {
        this.archivoContentType = archivoContentType;
        return this;
    }

    public void setArchivoContentType(String archivoContentType) {
        this.archivoContentType = archivoContentType;
    }

    public String getUrlDocumento() {
        return this.urlDocumento;
    }

    public Entrega urlDocumento(String urlDocumento) {
        this.setUrlDocumento(urlDocumento);
        return this;
    }

    public void setUrlDocumento(String urlDocumento) {
        this.urlDocumento = urlDocumento;
    }

    public Integer getVersion() {
        return this.version;
    }

    public Entrega version(Integer version) {
        this.setVersion(version);
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Instant getFechaEntrega() {
        return this.fechaEntrega;
    }

    public Entrega fechaEntrega(Instant fechaEntrega) {
        this.setFechaEntrega(fechaEntrega);
        return this;
    }

    public void setFechaEntrega(Instant fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public EstadoEntrega getEstado() {
        return this.estado;
    }

    public Entrega estado(EstadoEntrega estado) {
        this.setEstado(estado);
        return this;
    }

    public void setEstado(EstadoEntrega estado) {
        this.estado = estado;
    }

    public Set<Observacion> getObservacions() {
        return this.observacions;
    }

    public void setObservacions(Set<Observacion> observacions) {
        if (this.observacions != null) {
            this.observacions.forEach(i -> i.setEntrega(null));
        }
        if (observacions != null) {
            observacions.forEach(i -> i.setEntrega(this));
        }
        this.observacions = observacions;
    }

    public Entrega observacions(Set<Observacion> observacions) {
        this.setObservacions(observacions);
        return this;
    }

    public Entrega addObservacion(Observacion observacion) {
        this.observacions.add(observacion);
        observacion.setEntrega(this);
        return this;
    }

    public Entrega removeObservacion(Observacion observacion) {
        this.observacions.remove(observacion);
        observacion.setEntrega(null);
        return this;
    }

    public User getAutor() {
        return this.autor;
    }

    public void setAutor(User user) {
        this.autor = user;
    }

    public Entrega autor(User user) {
        this.setAutor(user);
        return this;
    }

    public ProyectoGrado getProyecto() {
        return this.proyecto;
    }

    public void setProyecto(ProyectoGrado proyectoGrado) {
        this.proyecto = proyectoGrado;
    }

    public Entrega proyecto(ProyectoGrado proyectoGrado) {
        this.setProyecto(proyectoGrado);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Entrega)) {
            return false;
        }
        return getId() != null && getId().equals(((Entrega) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Entrega{" +
            "id=" + getId() +
            ", etapa='" + getEtapa() + "'" +
            ", titulo='" + getTitulo() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", archivo='" + getArchivo() + "'" +
            ", archivoContentType='" + getArchivoContentType() + "'" +
            ", urlDocumento='" + getUrlDocumento() + "'" +
            ", version=" + getVersion() +
            ", fechaEntrega='" + getFechaEntrega() + "'" +
            ", estado='" + getEstado() + "'" +
            "}";
    }
}
