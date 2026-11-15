package co.edu.sgpg.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.*;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.*;
import co.edu.sgpg.security.AuthoritiesConstants;
import co.edu.sgpg.service.AvanceService;
import co.edu.sgpg.service.dto.workflow.ComentarioWorkflowDTO;
import co.edu.sgpg.service.mapper.EvaluacionMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** Contrato HTTP, permisos reales y persistencia del flujo académico. Requiere PostgreSQL/Testcontainers. */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "wf-admin", authorities = AuthoritiesConstants.ADMIN)
@Import(ProyectoWorkflowResourceIT.RelojFijo.class)
class ProyectoWorkflowResourceIT {

    private static final Instant AHORA = Instant.parse("2026-11-13T15:00:00Z");
    private static final String URL = "/api/proyecto-grados/{id}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private ProyectoGradoRepository proyectos;

    @Autowired
    private UserRepository usuarios;

    @Autowired
    private AuthorityRepository autoridades;

    @Autowired
    private EntregaRepository entregas;

    @Autowired
    private ObservacionRepository observaciones;

    @Autowired
    private EvaluacionRepository evaluaciones;

    @Autowired
    private FechaLimiteEtapaRepository fechas;

    @Autowired
    private HistorialEtapaRepository historial;

    @Autowired
    private EvaluacionMapper evaluacionMapper;

    @Autowired
    private AvanceService avance;

    @Autowired
    private EntityManager em;

    private ProyectoGrado proyecto;
    private User tutor;
    private User estudiante;
    private User juradoUno;
    private User juradoDos;

    @TestConfiguration(proxyBeanMethods = false)
    static class RelojFijo {

        @Bean
        @Primary
        Clock relojWorkflow() {
            return Clock.fixed(AHORA, ZoneOffset.UTC);
        }
    }

    @BeforeEach
    void preparar() {
        crearUsuario("wf-admin", AuthoritiesConstants.ADMIN);
        tutor = crearUsuario("wf-tutor", AuthoritiesConstants.TUTOR);
        estudiante = crearUsuario("wf-estudiante", AuthoritiesConstants.ESTUDIANTE);
        juradoUno = crearUsuario("wf-jurado-uno", AuthoritiesConstants.JURADO);
        juradoDos = crearUsuario("wf-jurado-dos", AuthoritiesConstants.JURADO);
        crearUsuario("wf-jurado-ajeno", AuthoritiesConstants.JURADO);
        proyecto = proyectos.saveAndFlush(
            new ProyectoGrado()
                .titulo("Seguimiento de un proyecto académico")
                .resumen("Proyecto ficticio de prueba")
                .programa("Ingeniería de sistemas")
                .etapaActual(Etapa.PROPUESTA)
                .estado(EstadoProyecto.ACTIVO)
                .porcentajeAvance(0)
                .fechaRegistro(AHORA)
                .tutor(tutor)
                .addEstudiante(estudiante)
                .addJurado(juradoUno)
                .addJurado(juradoDos)
        );
    }

    @Test
    void recorreTodasLasEtapasComoTutorYJuradosYPersisteHistorialYAvance() throws Exception {
        aprobarEntrega(Etapa.PROPUESTA);
        mvc.perform(
            get(URL + "/workflow", proyecto.getId()).with(user(estudiante.getLogin()).authorities(() -> AuthoritiesConstants.ESTUDIANTE))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.proyectoId").value(proyecto.getId().intValue()))
            .andExpect(jsonPath("$.etapaActual").value("PROPUESTA"))
            .andExpect(jsonPath("$.etapaSiguiente").value("ANTEPROYECTO"))
            .andExpect(jsonPath("$.puedeAvanzar").value(true))
            .andExpect(
                jsonPath(
                    "$.requisitos[*].codigo",
                    contains("TUTOR_ASIGNADO", "ESTUDIANTES_ASIGNADOS", "ENTREGA_APROBADA", "SIN_CORRECCIONES_PENDIENTES")
                )
            );
        avanzarComoTutor("ANTEPROYECTO", 28);

        aprobarEntrega(Etapa.ANTEPROYECTO);
        evaluarComoJurado(juradoUno, Etapa.ANTEPROYECTO, "4.0", Concepto.APROBADO);
        evaluarComoJurado(juradoDos, Etapa.ANTEPROYECTO, "3.5", Concepto.APROBADO_CON_MODIFICACIONES);
        avanzarComoTutor("DESARROLLO", 51);

        aprobarEntrega(Etapa.DESARROLLO);
        fechas.saveAndFlush(new FechaLimiteEtapa().proyecto(proyecto).etapa(Etapa.SUSTENTACION).fechaLimite(AHORA.plusSeconds(86400)));
        avanzarComoTutor("SUSTENTACION", 75);
        evaluarComoJurado(juradoUno, Etapa.SUSTENTACION, "4.5", Concepto.APROBADO);
        evaluarComoJurado(juradoDos, Etapa.SUSTENTACION, "4.0", Concepto.APROBADO);
        avanzarComoTutor("FINALIZADO", 100);

        Long id = proyecto.getId();
        em.flush();
        em.clear();
        var persistido = proyectos.findById(id).orElseThrow();
        assertThat(persistido.getEtapaActual()).isEqualTo(Etapa.FINALIZADO);
        assertThat(persistido.getEstado()).isEqualTo(EstadoProyecto.APROBADO);
        assertThat(persistido.getFechaActualizacion()).isEqualTo(AHORA);
        assertThat(persistido.getPorcentajeAvance()).isEqualTo(100);
        var registros = historial.findByProyectoIdOrderByFechaAsc(id);
        // El reloj fijo da la misma fecha a todos; el orden entre fechas iguales no está especificado.
        assertThat(registros)
            .hasSize(4)
            .allSatisfy(h -> {
                assertThat(h.getUsuario().getLogin()).isEqualTo(tutor.getLogin());
                assertThat(h.getFecha()).isEqualTo(AHORA);
                assertThat(h.getComentario()).isEqualTo("Etapa revisada por el tutor");
            });
        assertThat(registros)
            .extracting(HistorialEtapa::getEtapaOrigen)
            .containsExactlyInAnyOrder(Etapa.PROPUESTA, Etapa.ANTEPROYECTO, Etapa.DESARROLLO, Etapa.SUSTENTACION);
        assertThat(registros)
            .extracting(HistorialEtapa::getEtapaDestino)
            .containsExactlyInAnyOrder(Etapa.ANTEPROYECTO, Etapa.DESARROLLO, Etapa.SUSTENTACION, Etapa.FINALIZADO);
        mvc.perform(get(URL + "/seguimiento", id).with(user(juradoUno.getLogin()).authorities(() -> AuthoritiesConstants.JURADO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.proyecto.estado").value("APROBADO"))
            .andExpect(jsonPath("$.porcentajeAvance").value(100))
            .andExpect(jsonPath("$.etapas[*].etapa", contains("PROPUESTA", "ANTEPROYECTO", "DESARROLLO", "SUSTENTACION", "FINALIZADO")))
            .andExpect(jsonPath("$.etapas[*].estado", contains("COMPLETADA", "COMPLETADA", "COMPLETADA", "COMPLETADA", "COMPLETADA")))
            .andExpect(jsonPath("$.etapas[0].entregas[0].observacionesPendientes").value(0))
            .andExpect(jsonPath("$.etapas[1].evaluaciones.length()").value(2))
            .andExpect(jsonPath("$.etapas[3].diasRestantes").value(1))
            .andExpect(jsonPath("$.historial.length()").value(4))
            .andExpect(jsonPath("$.requisitos.etapaSiguiente").isEmpty())
            .andExpect(jsonPath("$.requisitos.puedeAvanzar").value(false))
            .andExpect(jsonPath("$.requisitos.requisitos").isEmpty());
    }

    @Test
    void estudianteNoPuedeAvanzarAunqueCumplaRequisitos() throws Exception {
        aprobarEntrega(Etapa.PROPUESTA);
        mvc.perform(
            post(URL + "/workflow/avanzar", proyecto.getId()).with(
                user(estudiante.getLogin()).authorities(() -> AuthoritiesConstants.ESTUDIANTE)
            )
        ).andExpect(status().isForbidden());
        assertSinTransicion();
    }

    @Test
    void juradoNoRelacionadoNoPuedeVerWorkflowNiSeguimiento() throws Exception {
        for (String ruta : new String[] { "/workflow", "/seguimiento" }) {
            mvc.perform(
                get(URL + ruta, proyecto.getId()).with(user("wf-jurado-ajeno").authorities(() -> AuthoritiesConstants.JURADO))
            ).andExpect(status().isForbidden());
        }
    }

    @Test
    void avanzarSinRequisitosDevuelve409ConCodigoYPendientesSinPersistirCambios() throws Exception {
        mvc.perform(
            post(URL + "/workflow/avanzar", proyecto.getId()).with(user(tutor.getLogin()).authorities(() -> AuthoritiesConstants.TUTOR))
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("REQUISITOS_PENDIENTES"))
            .andExpect(jsonPath("$.message").value("error.reglaNegocio"))
            .andExpect(jsonPath("$.pendientes", contains("ENTREGA_APROBADA")));
        assertSinTransicion();
    }

    @Test
    void correccionRequeridaBloqueaHastaResolverlaYSeIncluyeEnSeguimiento() throws Exception {
        var entrega = aprobarEntrega(Etapa.PROPUESTA);
        var correccion = observaciones.saveAndFlush(
            new Observacion()
                .entrega(entrega)
                .autor(tutor)
                .tipo(TipoObservacion.CORRECCION_REQUERIDA)
                .contenido("Ajustar objetivos")
                .resuelta(false)
                .fechaCreacion(AHORA)
        );
        mvc.perform(post(URL + "/workflow/avanzar", proyecto.getId()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.pendientes", contains("SIN_CORRECCIONES_PENDIENTES")));
        mvc.perform(get(URL + "/seguimiento", proyecto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.etapas[0].entregas[0].observacionesPendientes").value(1));
        correccion.setResuelta(true);
        observaciones.saveAndFlush(correccion);
        avanzarComoTutor("ANTEPROYECTO", 28);
    }

    @ParameterizedTest
    @CsvSource({ "2.9,REPROBADO", "5,REPROBADO", "2.9,APROBADO" })
    void finalizaReprobadoSegunPromedioOConcepto(String nota, Concepto concepto) throws Exception {
        proyecto.etapaActual(Etapa.SUSTENTACION);
        proyectos.saveAndFlush(proyecto);
        evaluaciones.saveAndFlush(
            new Evaluacion()
                .proyecto(proyecto)
                .jurado(juradoUno)
                .etapa(Etapa.SUSTENTACION)
                .calificacion(new BigDecimal(nota))
                .concepto(concepto)
                .fechaEvaluacion(AHORA)
        );
        evaluaciones.saveAndFlush(
            new Evaluacion()
                .proyecto(proyecto)
                .jurado(juradoDos)
                .etapa(Etapa.SUSTENTACION)
                .calificacion(new BigDecimal(nota))
                .concepto(Concepto.APROBADO)
                .fechaEvaluacion(AHORA)
        );
        avanzarComoTutor("FINALIZADO", 100);
        em.flush();
        em.clear();
        assertThat(proyectos.findById(proyecto.getId()).orElseThrow().getEstado()).isEqualTo(EstadoProyecto.REPROBADO);
    }

    @Test
    void soloAdminCancelaConComentarioYDejaHistorialEnLaMismaEtapa() throws Exception {
        mvc.perform(
            post(URL + "/workflow/cancelar", proyecto.getId())
                .with(user(tutor.getLogin()).authorities(() -> AuthoritiesConstants.TUTOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(new ComentarioWorkflowDTO("Retiro")))
        ).andExpect(status().isForbidden());
        mvc.perform(post(URL + "/workflow/cancelar", proyecto.getId()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("COMENTARIO_OBLIGATORIO"));
        mvc.perform(
            post(URL + "/workflow/cancelar", proyecto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comentario\":\"  \"}")
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("COMENTARIO_OBLIGATORIO"));
        assertSinTransicion();
        mvc.perform(
            post(URL + "/workflow/cancelar", proyecto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(new ComentarioWorkflowDTO("Retiro voluntario")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CANCELADO"));
        em.flush();
        em.clear();
        var guardado = proyectos.findById(proyecto.getId()).orElseThrow();
        assertThat(guardado.getEstado()).isEqualTo(EstadoProyecto.CANCELADO);
        assertThat(guardado.getFechaActualizacion()).isEqualTo(AHORA);
        assertThat(guardado.getPorcentajeAvance()).isEqualTo(15);
        assertThat(historial.findByProyectoIdOrderByFechaAsc(proyecto.getId()))
            .singleElement()
            .satisfies(h -> {
                assertThat(h.getEtapaOrigen()).isEqualTo(Etapa.PROPUESTA);
                assertThat(h.getEtapaDestino()).isEqualTo(Etapa.PROPUESTA);
                assertThat(h.getUsuario().getLogin()).isEqualTo("wf-admin");
                assertThat(h.getComentario()).isEqualTo("Retiro voluntario");
            });
        mvc.perform(post(URL + "/workflow/avanzar", proyecto.getId()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("PROYECTO_NO_ACTIVO"));
    }

    @Test
    void todasLasRutasDevuelven404SiNoExisteElProyecto() throws Exception {
        mvc.perform(get(URL + "/workflow", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(get(URL + "/seguimiento", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(post(URL + "/workflow/avanzar", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(
            post(URL + "/workflow/cancelar", Long.MAX_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comentario\":\"Motivo\"}")
        ).andExpect(status().isNotFound());
    }

    @Test
    void comentarioDemasiadoLargoDevuelve400SinMutar() throws Exception {
        mvc.perform(
            post(URL + "/workflow/cancelar", proyecto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(new ComentarioWorkflowDTO("x".repeat(501))))
        ).andExpect(status().isBadRequest());
        assertSinTransicion();
    }

    private User crearUsuario(String login, String rol) {
        var usuario = new User();
        usuario.setLogin(login);
        usuario.setPassword("x".repeat(60));
        usuario.setEmail(login + "@example.test");
        usuario.setActivated(true);
        usuario.setAuthorities(Set.of(autoridades.findById(rol).orElseThrow()));
        return usuarios.saveAndFlush(usuario);
    }

    private Entrega aprobarEntrega(Etapa etapa) {
        var entrega = entregas.saveAndFlush(
            new Entrega()
                .proyecto(proyecto)
                .autor(estudiante)
                .etapa(etapa)
                .titulo("Documento " + etapa)
                .version(1)
                .estado(EstadoEntrega.APROBADA)
                .fechaEntrega(AHORA)
        );
        avance.recalcular(proyecto.getId());
        return entrega;
    }

    private void evaluarComoJurado(User jurado, Etapa etapa, String nota, Concepto concepto) throws Exception {
        var dto = evaluacionMapper.toDto(
            new Evaluacion()
                .proyecto(proyecto)
                .jurado(jurado)
                .etapa(etapa)
                .calificacion(new BigDecimal(nota))
                .concepto(concepto)
                .fechaEvaluacion(AHORA)
        );
        mvc.perform(
            post("/api/evaluacions")
                .with(user(jurado.getLogin()).authorities(() -> AuthoritiesConstants.JURADO))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(dto))
        ).andExpect(status().isCreated());
    }

    private void avanzarComoTutor(String destino, int porcentajeEsperado) throws Exception {
        mvc.perform(
            post(URL + "/workflow/avanzar", proyecto.getId())
                .with(user(tutor.getLogin()).authorities(() -> AuthoritiesConstants.TUTOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(new ComentarioWorkflowDTO("Etapa revisada por el tutor")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.etapaActual").value(destino))
            .andExpect(jsonPath("$.porcentajeAvance").value(porcentajeEsperado));
    }

    private void assertSinTransicion() {
        em.flush();
        assertThat(proyectos.findById(proyecto.getId()).orElseThrow().getEtapaActual()).isEqualTo(Etapa.PROPUESTA);
        assertThat(historial.findByProyectoIdOrderByFechaAsc(proyecto.getId())).isEmpty();
    }
}
