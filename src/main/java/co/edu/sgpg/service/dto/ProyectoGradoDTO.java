package co.edu.sgpg.service.dto;

import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link co.edu.sgpg.domain.ProyectoGrado} entity.
 */
@Schema(description = "Proyecto de grado: unidad central del seguimiento.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProyectoGradoDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 10, max = 250)
    private String titulo;

    @Lob
    private String resumen;

    @NotNull
    @Size(max = 120)
    private String programa;

    @NotNull
    private Etapa etapaActual;

    @NotNull
    private EstadoProyecto estado;

    @Min(value = 0)
    @Max(value = 100)
    private Integer porcentajeAvance;

    @NotNull
    private Instant fechaRegistro;

    private Instant fechaActualizacion;

    private UserDTO tutor;

    private Set<UserDTO> estudianteses = new HashSet<>();

    private Set<UserDTO> juradoses = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public Etapa getEtapaActual() {
        return etapaActual;
    }

    public void setEtapaActual(Etapa etapaActual) {
        this.etapaActual = etapaActual;
    }

    public EstadoProyecto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProyecto estado) {
        this.estado = estado;
    }

    public Integer getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(Integer porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Instant fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public UserDTO getTutor() {
        return tutor;
    }

    public void setTutor(UserDTO tutor) {
        this.tutor = tutor;
    }

    public Set<UserDTO> getEstudianteses() {
        return estudianteses;
    }

    public void setEstudianteses(Set<UserDTO> estudianteses) {
        this.estudianteses = estudianteses;
    }

    public Set<UserDTO> getJuradoses() {
        return juradoses;
    }

    public void setJuradoses(Set<UserDTO> juradoses) {
        this.juradoses = juradoses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProyectoGradoDTO)) {
            return false;
        }

        ProyectoGradoDTO proyectoGradoDTO = (ProyectoGradoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, proyectoGradoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProyectoGradoDTO{" +
            "id=" + getId() +
            ", titulo='" + getTitulo() + "'" +
            ", resumen='" + getResumen() + "'" +
            ", programa='" + getPrograma() + "'" +
            ", etapaActual='" + getEtapaActual() + "'" +
            ", estado='" + getEstado() + "'" +
            ", porcentajeAvance=" + getPorcentajeAvance() +
            ", fechaRegistro='" + getFechaRegistro() + "'" +
            ", fechaActualizacion='" + getFechaActualizacion() + "'" +
            ", tutor=" + getTutor() +
            ", estudianteses=" + getEstudianteses() +
            ", juradoses=" + getJuradoses() +
            "}";
    }
}
