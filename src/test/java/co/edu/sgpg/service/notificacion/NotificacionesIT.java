package co.edu.sgpg.service.notificacion;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.EntregaRepository;
import co.edu.sgpg.repository.HistorialEtapaRepository;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.security.AuthoritiesConstants;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** PostgreSQL + REST + commit real + ejecutor asíncrono, sin SMTP externo. */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = { "sgpg.notificaciones.habilitadas=true", "sgpg.notificaciones.recordatorio-cron=-" })
@Import(NotificacionesIT.EjecutorAsincrono.class)
class NotificacionesIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProyectoGradoRepository proyectos;

    @Autowired
    private EntregaRepository entregas;

    @Autowired
    private HistorialEtapaRepository historial;

    @Autowired
    private UserRepository usuarios;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private ProyectoGrado proyecto;
    private Entrega entrega;
    private User estudiante;
    private User tutor;

    @TestConfiguration(proxyBeanMethods = false)
    @EnableAsync
    static class EjecutorAsincrono implements AsyncConfigurer {

        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        @Bean(destroyMethod = "close")
        ExecutorService notificacionesTestExecutor() {
            return executor;
        }

        @Override
        public Executor getAsyncExecutor() {
            return executor;
        }
    }

    @BeforeEach
    void preparar() {
        when(javaMailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage((Session) null));
        estudiante = crearUsuario("notificacion-estudiante");
        tutor = crearUsuario("notificacion-tutor");
        proyecto = proyectos.saveAndFlush(
            new ProyectoGrado()
                .titulo("Proyecto con correo")
                .resumen("Prueba de notificaciones")
                .programa("Ingeniería")
                .etapaActual(Etapa.PROPUESTA)
                .estado(EstadoProyecto.ACTIVO)
                .porcentajeAvance(0)
                .fechaRegistro(Instant.parse("2026-11-13T12:00:00Z"))
                .tutor(tutor)
                .addEstudiante(estudiante)
        );
        entrega = entregas.saveAndFlush(
            new Entrega()
                .titulo("Propuesta aprobada")
                .etapa(Etapa.PROPUESTA)
                .estado(EstadoEntrega.APROBADA)
                .version(1)
                .fechaEntrega(Instant.parse("2026-11-13T12:00:00Z"))
                .proyecto(proyecto)
                .autor(estudiante)
        );
    }

    @Test
    void avanzarPorRestEnviaElCorreoSoloTrasConfirmarLaTransaccion() throws Exception {
        avanzar();
        verify(javaMailSender, after(300).never()).send(any(MimeMessage.class));
        TestTransaction.flagForCommit();
        TestTransaction.end();
        var correo = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, timeout(5000).times(1)).send(correo.capture());
        assertThat(correo.getValue().getAllRecipients()).hasSize(1);
        assertThat(correo.getValue().getAllRecipients()[0]).hasToString(estudiante.getEmail());
        assertThat(correo.getValue().getSubject()).contains("cambio de etapa", "Proyecto con correo");
        assertThat(correo.getValue().getContent().toString()).contains("ANTEPROYECTO", "/seguimiento/" + proyecto.getId());
        assertThat(proyectos.findById(proyecto.getId()).orElseThrow().getEtapaActual()).isEqualTo(Etapa.ANTEPROYECTO);
    }

    @Test
    void rollbackDespuesDeAvanzarNoEnviaCorreo() throws Exception {
        avanzar();
        TestTransaction.flagForRollback();
        TestTransaction.end();
        verify(javaMailSender, after(500).never()).send(any(MimeMessage.class));
    }

    @Test
    void falloSmtpNoRevierteLaEtapaConfirmada() throws Exception {
        doThrow(new MailSendException("SMTP no disponible")).when(javaMailSender).send(any(MimeMessage.class));
        avanzar();
        TestTransaction.flagForCommit();
        TestTransaction.end();
        verify(javaMailSender, timeout(5000)).send(any(MimeMessage.class));
        assertThat(proyectos.findById(proyecto.getId()).orElseThrow().getEtapaActual()).isEqualTo(Etapa.ANTEPROYECTO);
        assertThat(historial.findByProyectoIdOrderByFechaAsc(proyecto.getId())).hasSize(1);
    }

    @AfterEach
    void limpiarDatosConfirmados() {
        if (TestTransaction.isActive()) TestTransaction.end();
        // Los tests de commit no se pueden aislar mediante el rollback habitual de @Transactional.
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            if (proyecto != null && proyectos.existsById(proyecto.getId())) {
                historial.deleteAll(historial.findByProyectoIdOrderByFechaAsc(proyecto.getId()));
                entregas.deleteById(entrega.getId());
                proyectos.deleteById(proyecto.getId());
                proyectos.flush();
            }
            if (estudiante != null) usuarios.findById(estudiante.getId()).ifPresent(usuarios::delete);
            if (tutor != null) usuarios.findById(tutor.getId()).ifPresent(usuarios::delete);
        });
    }

    private void avanzar() throws Exception {
        mvc.perform(
            post("/api/proyecto-grados/{id}/workflow/avanzar", proyecto.getId()).with(
                user(tutor.getLogin()).authorities(() -> AuthoritiesConstants.TUTOR)
            )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.etapaActual").value("ANTEPROYECTO"));
    }

    private User crearUsuario(String login) {
        User usuario = new User();
        usuario.setLogin(login);
        usuario.setEmail(login + "@example.test");
        usuario.setActivated(true);
        usuario.setPassword("x".repeat(60));
        return usuarios.saveAndFlush(usuario);
    }
}
