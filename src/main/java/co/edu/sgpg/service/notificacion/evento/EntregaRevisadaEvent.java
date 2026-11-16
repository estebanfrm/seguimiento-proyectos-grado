package co.edu.sgpg.service.notificacion.evento;

import co.edu.sgpg.domain.enumeration.EstadoEntrega;

public record EntregaRevisadaEvent(Long proyectoId, Long entregaId, EstadoEntrega estado, String autorLogin) {}
