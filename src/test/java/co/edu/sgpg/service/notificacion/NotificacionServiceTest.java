package co.edu.sgpg.service.notificacion;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.service.MailService;
import co.edu.sgpg.service.notificacion.evento.*;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import tech.jhipster.config.JHipsterProperties;

/** Destinatarios y renderizado de las siete plantillas reales, sin Spring ni base de datos. */
@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private ProyectoGradoRepository proyectos;

    @Mock
    private MailService mail;

    private NotificacionService servicio;
    private ProyectoGrado proyecto;
    private User estudiante;
    private User tutor;
    private User jurado;
    private SpringTemplateEngine plantillas;
    private ResourceBundleMessageSource mensajes;
    private JHipsterProperties propiedades;

    @BeforeEach
    void preparar() {
        estudiante = usuario(1L, "estudiante");
        tutor = usuario(2L, "tutor");
        jurado = usuario(3L, "jurado");
        proyecto = new ProyectoGrado()
            .id(10L)
            .titulo("Investigación académica")
            .estado(EstadoProyecto.ACTIVO)
            .etapaActual(Etapa.PROPUESTA)
            .tutor(tutor)
            .addEstudiante(estudiante)
            .addJurado(jurado);
        mensajes = new ResourceBundleMessageSource();
        mensajes.setBasename("i18n/messages");
        mensajes.setDefaultEncoding("UTF-8");
        mensajes.setFallbackToSystemLocale(false);
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        plantillas = new SpringTemplateEngine();
        plantillas.setTemplateResolver(resolver);
        plantillas.setMessageSource(mensajes);
        propiedades = new JHipsterProperties();
        propiedades.getMail().setBaseUrl("https://sgpg.example/");
        servicio = new NotificacionService(proyectos, mail, plantillas, mensajes, propiedades, true);
    }

    @Test
    void entregaRegistradaSoloAvisaAlTutor() {
        cargarProyecto();
        servicio.entregaRegistrada(new EntregaRegistradaEvent(10L, 20L, estudiante.getLogin()));
        assertCorreos("tutor@example.test");
    }

    @Test
    void entregaRevisadaSoloAvisaAEstudiantesConElEstadoDeLaRevision() {
        cargarProyecto();
        servicio.entregaRevisada(new EntregaRevisadaEvent(10L, 20L, EstadoEntrega.APROBADA, tutor.getLogin()));
        assertCorreos("estudiante@example.test");
        assertThat(contenido()).contains("APROBADA", "20");
    }

    @Test
    void observacionSoloAvisaAEstudiantes() {
        cargarProyecto();
        servicio.observacionRegistrada(new ObservacionRegistradaEvent(10L, 30L, tutor.getLogin()));
        assertCorreos("estudiante@example.test");
        assertThat(contenido()).contains("observación", "30");
    }

    @Test
    void evaluacionAvisaAEstudiantesYTutor() {
        cargarProyecto();
        servicio.evaluacionRegistrada(new EvaluacionRegistradaEvent(10L, 40L, jurado.getLogin()));
        assertCorreos("estudiante@example.test", "tutor@example.test");
    }

    @Test
    void avanceAvisaAEstudiantesYJuradosExcluyendoAlTutorQueActua() {
        cargarProyecto();
        servicio.etapaAvanzada(new EtapaAvanzadaEvent(10L, Etapa.PROPUESTA, Etapa.ANTEPROYECTO, EstadoProyecto.ACTIVO, tutor.getLogin()));
        assertCorreos("estudiante@example.test", "jurado@example.test");
        assertThat(contenido()).contains("PROPUESTA", "ANTEPROYECTO");
    }

    @ParameterizedTest
    @EnumSource(value = EstadoProyecto.class, names = { "APROBADO", "REPROBADO" })
    void finalizacionIncluyeElResultadoCapturadoEnElEvento(EstadoProyecto resultado) {
        cargarProyecto();
        servicio.etapaAvanzada(new EtapaAvanzadaEvent(10L, Etapa.SUSTENTACION, Etapa.FINALIZADO, resultado, "coordinador"));
        assertCorreos("estudiante@example.test", "tutor@example.test", "jurado@example.test");
        assertThat(contenido()).contains("Resultado final: " + resultado);
    }

    @Test
    void cancelacionAvisaATodosConElMotivoEscapado() {
        cargarProyecto();
        servicio.proyectoCancelado(new ProyectoCanceladoEvent(10L, "Retiro <script>alert(1)</script>", "coordinador"));
        assertCorreos("estudiante@example.test", "tutor@example.test", "jurado@example.test");
        assertThat(contenido()).contains("Retiro &lt;script&gt;").doesNotContain("<script>");
    }

    @Test
    void nuncaNotificaAlAutorAunqueSeaEstudianteYEsteEnVariosRoles() {
        cargarProyecto();
        proyecto.setTutor(estudiante);
        proyecto.addJurado(estudiante);
        servicio.proyectoCancelado(new ProyectoCanceladoEvent(10L, "Retiro", "ESTUDIANTE"));
        assertCorreos("jurado@example.test");
    }

    @Test
    void omiteUsuariosInactivos() {
        cargarProyecto();
        estudiante.setActivated(false);
        tutor.setActivated(false);
        servicio.proyectoCancelado(new ProyectoCanceladoEvent(10L, "Retiro", "coordinador"));
        assertCorreos("jurado@example.test");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "  " })
    void omiteUsuariosSinCorreo(String correo) {
        cargarProyecto();
        estudiante.setEmail(correo);
        tutor.setEmail(correo);
        servicio.proyectoCancelado(new ProyectoCanceladoEvent(10L, "Retiro", "coordinador"));
        assertCorreos("jurado@example.test");
    }

    @Test
    void noDuplicaCorreosSiUnaPersonaTieneVariosRoles() {
        cargarProyecto();
        proyecto.setJurados(Set.of(tutor));
        proyecto.addEstudiante(tutor);
        servicio.proyectoCancelado(new ProyectoCanceladoEvent(10L, "Retiro", "coordinador"));
        assertCorreos("estudiante@example.test", "tutor@example.test");
    }

    @Test
    void entregaSinTutorNoEnviaCorreos() {
        cargarProyecto();
        proyecto.setTutor(null);
        servicio.entregaRegistrada(new EntregaRegistradaEvent(10L, 20L, estudiante.getLogin()));
        verifyNoInteractions(mail);
    }

    @Test
    void proyectoEliminadoNoEnviaCorreos() {
        when(proyectos.findById(10L)).thenReturn(Optional.empty());
        servicio.entregaRegistrada(new EntregaRegistradaEvent(10L, 20L, estudiante.getLogin()));
        verifyNoInteractions(mail);
    }

    @Test
    void recordatorioAvisaSoloAEstudiantesYTutor() {
        cargarProyecto();
        servicio.recordarFechaLimite(10L, Etapa.PROPUESTA, Instant.parse("2026-11-16T23:59:00Z"), 3);
        assertCorreos("estudiante@example.test", "tutor@example.test");
        assertThat(contenido()).contains("3 día(s)", "2026-11-16T23:59:00Z");
    }

    @Test
    void recordatorioNoAvisaSiElProyectoYaCambioDeEtapa() {
        cargarProyecto();
        servicio.recordarFechaLimite(10L, Etapa.ANTEPROYECTO, Instant.EPOCH, 1);
        verifyNoInteractions(mail);
    }

    @Test
    void recordatorioNoAvisaSiElProyectoYaFueCancelado() {
        cargarProyecto();
        proyecto.setEstado(EstadoProyecto.CANCELADO);
        servicio.recordarFechaLimite(10L, Etapa.PROPUESTA, Instant.EPOCH, 1);
        verifyNoInteractions(mail);
    }

    @Test
    void deshabilitadasNoConsultanNiEnvianParaNingunEvento() {
        servicio = new NotificacionService(proyectos, mail, plantillas, mensajes, propiedades, false);
        servicio.entregaRegistrada(new EntregaRegistradaEvent(10L, 20L, "estudiante"));
        servicio.entregaRevisada(new EntregaRevisadaEvent(10L, 20L, EstadoEntrega.APROBADA, "tutor"));
        servicio.observacionRegistrada(new ObservacionRegistradaEvent(10L, 30L, "tutor"));
        servicio.evaluacionRegistrada(new EvaluacionRegistradaEvent(10L, 40L, "jurado"));
        servicio.etapaAvanzada(new EtapaAvanzadaEvent(10L, Etapa.PROPUESTA, Etapa.ANTEPROYECTO, EstadoProyecto.ACTIVO, "tutor"));
        servicio.proyectoCancelado(new ProyectoCanceladoEvent(10L, "Retiro", "coordinador"));
        servicio.recordarFechaLimite(10L, Etapa.PROPUESTA, Instant.EPOCH, 1);
        verifyNoInteractions(proyectos, mail);
    }

    @Test
    void falloDeUnCorreoPermitePrepararLosOtrosDestinatarios() {
        cargarProyecto();
        doThrow(new IllegalStateException("Ejecutor no disponible"))
            .when(mail)
            .sendEmail(eq("estudiante@example.test"), anyString(), anyString(), eq(false), eq(true));
        assertThatCode(() -> servicio.evaluacionRegistrada(new EvaluacionRegistradaEvent(10L, 40L, "jurado"))).doesNotThrowAnyException();
        verify(mail).sendEmail(eq("tutor@example.test"), anyString(), anyString(), eq(false), eq(true));
    }

    private void cargarProyecto() {
        when(proyectos.findById(10L)).thenReturn(Optional.of(proyecto));
    }

    private void assertCorreos(String... destinatarios) {
        var correos = ArgumentCaptor.forClass(String.class);
        var contenido = ArgumentCaptor.forClass(String.class);
        verify(mail, times(destinatarios.length)).sendEmail(
            correos.capture(),
            contains("Investigación académica"),
            contenido.capture(),
            eq(false),
            eq(true)
        );
        assertThat(correos.getAllValues()).containsExactlyInAnyOrder(destinatarios);
        assertThat(contenido.getAllValues()).allSatisfy(html ->
            assertThat(html).contains("https://sgpg.example/seguimiento/10").doesNotContain("??")
        );
        verifyNoMoreInteractions(mail);
    }

    private String contenido() {
        var contenido = ArgumentCaptor.forClass(String.class);
        verify(mail, atLeastOnce()).sendEmail(anyString(), anyString(), contenido.capture(), eq(false), eq(true));
        return contenido.getValue();
    }

    private static User usuario(Long id, String login) {
        var usuario = new User();
        usuario.setId(id);
        usuario.setLogin(login);
        usuario.setEmail(login + "@example.test");
        usuario.setActivated(true);
        return usuario;
    }
}
