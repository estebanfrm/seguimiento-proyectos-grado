package co.edu.sgpg.service.notificacion.evento;

public record ProyectoCanceladoEvent(Long proyectoId, String motivo, String autorLogin) {}
