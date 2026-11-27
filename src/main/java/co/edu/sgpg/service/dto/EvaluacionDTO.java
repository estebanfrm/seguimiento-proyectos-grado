package co.edu.sgpg.service.dto;

import co.edu.sgpg.domain.enumeration.Concepto;
import co.edu.sgpg.domain.enumeration.Etapa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link co.edu.sgpg.domain.Evaluacion} entity.
 */
@Schema(description = "Evaluación de un jurado en una etapa evaluable.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EvaluacionDTO implements Serializable {

    private Long id;

    private Etapa etapa;

    @NotNull
    @DecimalMin(value = "0")
    @DecimalMax(value = "5")
    private BigDecimal calificacion;

    @NotNull
    private Concepto concepto;

    @Lob
    private String comentarios;

    private Instant fechaEvaluacion;

    private UserDTO jurado;

    @NotNull
    private ProyectoGradoDTO proyecto;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }

    public BigDecimal getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(BigDecimal calificacion) {
        this.calificacion = calificacion;
    }

    public Concepto getConcepto() {
        return concepto;
    }

    public void setConcepto(Concepto concepto) {
        this.concepto = concepto;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public Instant getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(Instant fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public UserDTO getJurado() {
        return jurado;
    }

    public void setJurado(UserDTO jurado) {
        this.jurado = jurado;
    }

    public ProyectoGradoDTO getProyecto() {
        return proyecto;
    }

    public void setProyecto(ProyectoGradoDTO proyecto) {
        this.proyecto = proyecto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EvaluacionDTO)) {
            return false;
        }

        EvaluacionDTO evaluacionDTO = (EvaluacionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, evaluacionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EvaluacionDTO{" +
            "id=" + getId() +
            ", etapa='" + getEtapa() + "'" +
            ", calificacion=" + getCalificacion() +
            ", concepto='" + getConcepto() + "'" +
            ", comentarios='" + getComentarios() + "'" +
            ", fechaEvaluacion='" + getFechaEvaluacion() + "'" +
            ", jurado=" + getJurado() +
            ", proyecto=" + getProyecto() +
            "}";
    }
}
