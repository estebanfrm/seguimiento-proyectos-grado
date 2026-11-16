package co.edu.sgpg.service.dto;

import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.domain.enumeration.Etapa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link co.edu.sgpg.domain.HistorialEtapa} entity.
 */
@Schema(description = "Bitácora de transiciones de etapa.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HistorialEtapaDTO implements Serializable {

    private Long id;

    private Etapa etapaOrigen;

    @NotNull
    private Etapa etapaDestino;

    @NotNull
    private Instant fecha;

    @Size(max = 500)
    private String comentario;

    private UserDTO usuario;

    @NotNull
    private ProyectoGradoDTO proyecto;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Etapa getEtapaOrigen() {
        return etapaOrigen;
    }

    public void setEtapaOrigen(Etapa etapaOrigen) {
        this.etapaOrigen = etapaOrigen;
    }

    public Etapa getEtapaDestino() {
        return etapaDestino;
    }

    public void setEtapaDestino(Etapa etapaDestino) {
        this.etapaDestino = etapaDestino;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public UserDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UserDTO usuario) {
        this.usuario = usuario;
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
        if (!(o instanceof HistorialEtapaDTO)) {
            return false;
        }

        HistorialEtapaDTO historialEtapaDTO = (HistorialEtapaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, historialEtapaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HistorialEtapaDTO{" +
            "id=" + getId() +
            ", etapaOrigen='" + getEtapaOrigen() + "'" +
            ", etapaDestino='" + getEtapaDestino() + "'" +
            ", fecha='" + getFecha() + "'" +
            ", comentario='" + getComentario() + "'" +
            ", usuario=" + getUsuario() +
            ", proyecto=" + getProyecto() +
            "}";
    }
}
