package co.edu.sgpg.service.dto;

/** Contenido binario reservado para el endpoint de descarga, nunca para respuestas JSON. */
public record ArchivoEntregaDTO(byte[] contenido, String nombre) {}
