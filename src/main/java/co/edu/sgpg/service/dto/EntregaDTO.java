package co.edu.sgpg.service.dto;

import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.Etapa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link co.edu.sgpg.domain.Entrega} entity.
 */
@Schema(description = "Documento entregado por el estudiante en una etapa.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EntregaDTO implements Serializable {

    private Long id;

    @NotNull
    private Etapa etapa;

    @NotNull
    @Size(max = 200)
    private String titulo;

    @Lob
    private String descripcion;

    @Lob
    private byte[] archivo;

    private String archivoContentType;

    @Size(max = 500)
    private String urlDocumento;

    @NotNull
    @Min(value = 1)
    private Integer version;

    @NotNull
    private Instant fechaEntrega;

    @NotNull
    private EstadoEntrega estado;

    private UserDTO autor;

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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public byte[] getArchivo() {
        return archivo;
    }

    public void setArchivo(byte[] archivo) {
        this.archivo = archivo;
    }

    public String getArchivoContentType() {
        return archivoContentType;
    }

    public void setArchivoContentType(String archivoContentType) {
        this.archivoContentType = archivoContentType;
    }

    public String getUrlDocumento() {
        return urlDocumento;
    }

    public void setUrlDocumento(String urlDocumento) {
        this.urlDocumento = urlDocumento;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Instant getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Instant fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public EstadoEntrega getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntrega estado) {
        this.estado = estado;
    }

    public UserDTO getAutor() {
        return autor;
    }

    public void setAutor(UserDTO autor) {
        this.autor = autor;
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
        if (!(o instanceof EntregaDTO)) {
            return false;
        }

        EntregaDTO entregaDTO = (EntregaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, entregaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EntregaDTO{" +
            "id=" + getId() +
            ", etapa='" + getEtapa() + "'" +
            ", titulo='" + getTitulo() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", archivo='" + getArchivo() + "'" +
            ", urlDocumento='" + getUrlDocumento() + "'" +
            ", version=" + getVersion() +
            ", fechaEntrega='" + getFechaEntrega() + "'" +
            ", estado='" + getEstado() + "'" +
            ", autor=" + getAutor() +
            ", proyecto=" + getProyecto() +
            "}";
    }
}
