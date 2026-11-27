package co.edu.sgpg.service.dto;

import co.edu.sgpg.domain.enumeration.TipoObservacion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link co.edu.sgpg.domain.Observacion} entity.
 */
@Schema(description = "Observación del tutor sobre una entrega.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ObservacionDTO implements Serializable {

    private Long id;

    @Lob
    private String contenido;

    @NotNull
    private TipoObservacion tipo;

    private Boolean resuelta;

    private Instant fechaCreacion;

    private UserDTO autor;

    @NotNull
    private EntregaDTO entrega;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public TipoObservacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoObservacion tipo) {
        this.tipo = tipo;
    }

    public Boolean getResuelta() {
        return resuelta;
    }

    public void setResuelta(Boolean resuelta) {
        this.resuelta = resuelta;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public UserDTO getAutor() {
        return autor;
    }

    public void setAutor(UserDTO autor) {
        this.autor = autor;
    }

    public EntregaDTO getEntrega() {
        return entrega;
    }

    public void setEntrega(EntregaDTO entrega) {
        this.entrega = entrega;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ObservacionDTO)) {
            return false;
        }

        ObservacionDTO observacionDTO = (ObservacionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, observacionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ObservacionDTO{" +
            "id=" + getId() +
            ", contenido='" + getContenido() + "'" +
            ", tipo='" + getTipo() + "'" +
            ", resuelta='" + getResuelta() + "'" +
            ", fechaCreacion='" + getFechaCreacion() + "'" +
            ", autor=" + getAutor() +
            ", entrega=" + getEntrega() +
            "}";
    }
}
