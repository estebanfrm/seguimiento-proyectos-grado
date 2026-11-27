package co.edu.sgpg.service.notificacion;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.service.MailService;
import co.edu.sgpg.service.notificacion.evento.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

/** Carga los destinatarios después del commit, sin trasladar entidades JPA al ejecutor asíncrono. */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
public class NotificacionService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionService.class);
    private static final Locale ESPANOL = Locale.forLanguageTag("es");

    private final ProyectoGradoRepository proyectos;
    private final MailService mail;
    private final SpringTemplateEngine plantillas;
    private final MessageSource mensajes;
    private final JHipsterProperties propiedades;
    private final boolean habilitadas;

    private enum Destinatarios {
        TUTOR,
        ESTUDIANTES,
        ESTUDIANTES_Y_TUTOR,
        TODOS,
    }

    public NotificacionService(
        ProyectoGradoRepository proyectos,
        MailService mail,
        SpringTemplateEngine plantillas,
        MessageSource mensajes,
        JHipsterProperties propiedades,
        @Value("${sgpg.notificaciones.habilitadas:true}") boolean habilitadas
    ) {
        this.proyectos = proyectos;
        this.mail = mail;
        this.plantillas = plantillas;
        this.mensajes = mensajes;
        this.propiedades = propiedades;
        this.habilitadas = habilitadas;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void entregaRegistrada(EntregaRegistradaEvent evento) {
        notificar(evento.proyectoId(), evento.autorLogin(), Destinatarios.TUTOR, "entregaRegistrada", Map.of("evento", evento));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void entregaRevisada(EntregaRevisadaEvent evento) {
        notificar(evento.proyectoId(), evento.autorLogin(), Destinatarios.ESTUDIANTES, "entregaRevisada", Map.of("evento", evento));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void observacionRegistrada(ObservacionRegistradaEvent evento) {
        notificar(evento.proyectoId(), evento.autorLogin(), Destinatarios.ESTUDIANTES, "observacionRegistrada", Map.of("evento", evento));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evaluacionRegistrada(EvaluacionRegistradaEvent evento) {
        notificar(
            evento.proyectoId(),
            evento.autorLogin(),
            Destinatarios.ESTUDIANTES_Y_TUTOR,
            "evaluacionRegistrada",
            Map.of("evento", evento)
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void etapaAvanzada(EtapaAvanzadaEvent evento) {
        notificar(evento.proyectoId(), evento.autorLogin(), Destinatarios.TODOS, "etapaAvanzada", Map.of("evento", evento));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void proyectoCancelado(ProyectoCanceladoEvent evento) {
        notificar(evento.proyectoId(), evento.autorLogin(), Destinatarios.TODOS, "proyectoCancelado", Map.of("evento", evento));
    }

    public void recordarFechaLimite(Long proyectoId, Etapa etapa, Instant fechaLimite, long diasRestantes) {
        if (!habilitadas) return;
        proyectos
            .findById(proyectoId)
            .filter(p -> p.getEstado() == EstadoProyecto.ACTIVO && p.getEtapaActual() == etapa)
            .ifPresent(proyecto ->
                enviar(
                    proyecto,
                    null,
                    Destinatarios.ESTUDIANTES_Y_TUTOR,
                    "recordatorio",
                    Map.of("etapa", etapa, "fechaLimite", fechaLimite, "diasRestantes", diasRestantes)
                )
            );
    }

    private void notificar(Long proyectoId, String autorLogin, Destinatarios destinatarios, String tipo, Map<String, Object> variables) {
        if (!habilitadas) return;
        proyectos.findById(proyectoId).ifPresent(proyecto -> enviar(proyecto, autorLogin, destinatarios, tipo, variables));
    }

    private void enviar(
        ProyectoGrado proyecto,
        String autorLogin,
        Destinatarios destinatarios,
        String tipo,
        Map<String, Object> variables
    ) {
        Stream<User> estudiantes = proyecto.getEstudiantes().stream();
        Stream<User> tutor = Stream.ofNullable(proyecto.getTutor());
        Stream<User> receptores = switch (destinatarios) {
            case TUTOR -> tutor;
            case ESTUDIANTES -> estudiantes;
            case ESTUDIANTES_Y_TUTOR -> Stream.concat(estudiantes, tutor);
            case TODOS -> Stream.concat(Stream.concat(estudiantes, tutor), proyecto.getJurados().stream());
        };
        Set<String> correosEnviados = new HashSet<>();
        receptores
            .filter(User::isActivated)
            .filter(usuario -> autorLogin == null || !autorLogin.equalsIgnoreCase(usuario.getLogin()))
            .filter(usuario -> usuario.getEmail() != null && !usuario.getEmail().isBlank())
            .filter(usuario -> correosEnviados.add(usuario.getEmail().toLowerCase(Locale.ROOT)))
            .forEach(usuario -> {
                try {
                    Context contexto = new Context(ESPANOL);
                    contexto.setVariables(variables);
                    contexto.setVariable("login", usuario.getLogin());
                    contexto.setVariable("tituloProyecto", proyecto.getTitulo());
                    contexto.setVariable(
                        "enlaceProyecto",
                        propiedades.getMail().getBaseUrl().replaceAll("/+$", "") + "/seguimiento/" + proyecto.getId()
                    );
                    String contenido = plantillas.process("mail/notificacion/" + tipo, contexto);
                    String asunto = mensajes.getMessage(
                        "email.notificacion." + tipo + ".title",
                        new Object[] { proyecto.getTitulo() },
                        ESPANOL
                    );
                    mail.sendEmail(usuario.getEmail(), asunto, contenido, false, true);
                } catch (RuntimeException e) {
                    LOG.warn(
                        "No se pudo preparar el correo {} del proyecto {} para el usuario {}",
                        tipo,
                        proyecto.getId(),
                        usuario.getId(),
                        e
                    );
                }
            });
    }
}
