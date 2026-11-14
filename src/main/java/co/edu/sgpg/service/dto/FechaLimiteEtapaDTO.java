package co.edu.sgpg.service.dto;

import co.edu.sgpg.domain.enumeration.Etapa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link co.edu.sgpg.domain.FechaLimiteEtapa} entity.
 */
@Schema(description = "Fecha límite de una etapa para un proyecto.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FechaLimiteEtapaDTO implements Serializable {

    private Long id;

    @NotNull
    private Etapa etapa;

    @NotNull
    private Instant fechaLimite;

    @Size(max = 250)
    private String descripcion;

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

    public Instant getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(Instant fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
        if (!(o instanceof FechaLimiteEtapaDTO)) {
            return false;
        }

        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = (FechaLimiteEtapaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, fechaLimiteEtapaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FechaLimiteEtapaDTO{" +
            "id=" + getId() +
            ", etapa='" + getEtapa() + "'" +
            ", fechaLimite='" + getFechaLimite() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", proyecto=" + getProyecto() +
            "}";
    }
}
