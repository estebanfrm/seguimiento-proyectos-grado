package co.edu.sgpg.service.notificacion.evento;

import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;

public record EtapaAvanzadaEvent(Long proyectoId, Etapa origen, Etapa destino, EstadoProyecto estado, String autorLogin) {}
