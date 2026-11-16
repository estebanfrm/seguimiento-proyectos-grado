package co.edu.sgpg.web.rest;

import static co.edu.sgpg.security.AuthoritiesConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.*;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** Matriz de permisos, consultas de visibilidad y conflictos del contrato del stream B. */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@Import(PermisosYReglasIT.RelojDePrueba.class)
class PermisosYReglasIT {

    private static final Instant AHORA = Instant.parse("2026-11-13T12:00:00Z");
    private static final String PREFIJO = "permisos-";

    @TestConfiguration(proxyBeanMethods = false)
    static class RelojDePrueba {

        @Bean
        @Primary
        Clock relojFijo() {
            return Clock.fixed(AHORA, ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository usuarios;

    @Autowired
    private ProyectoGradoRepository proyectos;

    @Autowired
    private EntregaRepository entregas;

    @Autowired
    private ObservacionRepository observaciones;

    @Autowired
    private EvaluacionRepository evaluaciones;

    @Autowired
    private FechaLimiteEtapaRepository fechas;

    @Autowired
    private HistorialEtapaRepository historiales;

    private User estudiante;
    private User tutor;
    private User jurado;
    private ProyectoGrado proyecto;
    private ProyectoGrado ajeno;

    @BeforeEach
    void preparar() {
        crearUsuario("admin", ADMIN, true);
        estudiante = crearUsuario("estudiante", ESTUDIANTE, true);
        tutor = crearUsuario("tutor", TUTOR, true);
        jurado = crearUsuario("jurado", JURADO, true);
        crearUsuario("user", USER, true);
        crearUsuario("otro-estudiante", ESTUDIANTE, true);
        crearUsuario("otro-tutor", TUTOR, true);
        crearUsuario("otro-jurado", JURADO, true);
        proyecto = proyectos.saveAndFlush(nuevoProyecto().addEstudiante(estudiante).tutor(tutor).addJurado(jurado));
        ajeno = proyectos.saveAndFlush(nuevoProyecto());
    }

    private User crearUsuario(String login, String rol, boolean activo) {
        Authority authority = em.find(Authority.class, rol);
        if (authority == null) {
            authority = new Authority().name(rol);
            em.persist(authority);
        }
        User usuario = new User();
        usuario.setLogin(PREFIJO + login);
        usuario.setPassword("x".repeat(60));
        usuario.setActivated(activo);
        usuario.setFirstName("Nombre " + login);
        usuario.setLastName("Apellido");
        usuario.setEmail(PREFIJO + login + "@example.test");
        usuario.setAuthorities(Set.of(authority));
        return usuarios.saveAndFlush(usuario);
    }

    private ProyectoGrado nuevoProyecto() {
        return new ProyectoGrado()
            .titulo("Proyecto de permisos")
            .resumen("Resumen académico")
            .programa("Ingeniería")
            .etapaActual(Etapa.PROPUESTA)
            .estado(EstadoProyecto.ACTIVO)
            .porcentajeAvance(0)
            .fechaRegistro(Instant.EPOCH);
    }

    private Entrega crearEntrega(ProyectoGrado p, EstadoEntrega estado) {
        return entregas.saveAndFlush(
            new Entrega()
                .proyecto(p)
                .autor(estudiante)
                .etapa(p.getEtapaActual())
                .titulo("Documento de prueba")
                .version(1)
                .estado(estado)
                .fechaEntrega(Instant.EPOCH)
        );
    }

    private Observacion crearObservacion(Entrega entrega) {
        return observaciones.saveAndFlush(
            new Observacion()
                .entrega(entrega)
                .autor(tutor)
                .contenido("Revisar referencias")
                .tipo(TipoObservacion.CORRECCION_REQUERIDA)
                .resuelta(false)
                .fechaCreacion(Instant.EPOCH)
        );
    }

    private Map<String, Object> cuerpoEntrega() {
        return Map.of(
            "titulo",
            "Documento enviado",
            "urlDocumento",
            "https://example.org/documento.pdf",
            "proyecto",
            Map.of("id", proyecto.getId()),
            "etapa",
            "FINALIZADO",
            "version",
            99,
            "estado",
            "APROBADA",
            "fechaEntrega",
            Instant.EPOCH.toString(),
            "autor",
            Map.of("id", tutor.getId())
        );
    }

    private Map<String, Object> cuerpoEvaluacion() {
        return Map.of(
            "proyecto",
            Map.of("id", proyecto.getId()),
            "calificacion",
            4,
            "concepto",
            "APROBADO",
            "etapa",
            "PROPUESTA",
            "jurado",
            Map.of("id", tutor.getId()),
            "fechaEvaluacion",
            Instant.EPOCH.toString()
        );
    }

    private Map<String, Object> cuerpoProyecto() {
        return Map.of(
            "titulo",
            "Propuesta registrada",
            "resumen",
            "Resumen",
            "programa",
            "Ingeniería",
            "etapaActual",
            "FINALIZADO",
            "estado",
            "APROBADO",
            "porcentajeAvance",
            100,
            "fechaRegistro",
            Instant.EPOCH.toString()
        );
    }

    private ResultActions como(String login, String rol, MockHttpServletRequestBuilder peticion) throws Exception {
        return mvc.perform(peticion.with(user(PREFIJO + login).authorities(() -> rol)));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, Object cuerpo) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cuerpo));
    }

    private void conflicto(ResultActions resultado, String codigo) throws Exception {
        resultado
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value(codigo))
            .andExpect(jsonPath("$.message").value("error.reglaNegocio"));
    }

    @ParameterizedTest
    @CsvSource({
        "admin,ROLE_ADMIN,201",
        "estudiante,ROLE_ESTUDIANTE,201",
        "tutor,ROLE_TUTOR,403",
        "jurado,ROLE_JURADO,403",
        "user,ROLE_USER,403",
    })
    void crearPropuestaSegunRol(String login, String rol, int esperado) throws Exception {
        var resultado = como(login, rol, json(post("/api/proyecto-grados"), cuerpoProyecto())).andExpect(status().is(esperado));
        if (esperado == 201) {
            resultado
                .andExpect(jsonPath("$.etapaActual").value("PROPUESTA"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.porcentajeAvance").value(0))
                .andExpect(jsonPath("$.fechaRegistro").value(AHORA.toString()));
        }
    }

    @Test
    void estudianteCreadorNoPuedeAsignarPersonalNiOtrosEstudiantes() throws Exception {
        var cuerpo = new LinkedHashMap<>(cuerpoProyecto());
        cuerpo.put("tutor", Map.of("id", tutor.getId()));
        cuerpo.put("jurados", List.of(Map.of("id", jurado.getId())));
        cuerpo.put("estudiantes", List.of(Map.of("id", tutor.getId())));
        como("estudiante", ESTUDIANTE, json(post("/api/proyecto-grados"), cuerpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tutor").isEmpty())
            .andExpect(jsonPath("$.jurados").isEmpty())
            .andExpect(jsonPath("$.estudiantes", hasSize(1)))
            .andExpect(jsonPath("$.estudiantes[0].id").value(estudiante.getId().intValue()));
    }

    @ParameterizedTest
    @CsvSource({
        "admin,ROLE_ADMIN,403",
        "estudiante,ROLE_ESTUDIANTE,201",
        "tutor,ROLE_TUTOR,403",
        "jurado,ROLE_JURADO,403",
        "user,ROLE_USER,403",
        "otro-estudiante,ROLE_ESTUDIANTE,403",
    })
    void crearEntregaSegunRolYRelacion(String login, String rol, int esperado) throws Exception {
        var resultado = como(login, rol, json(post("/api/entregas"), cuerpoEntrega())).andExpect(status().is(esperado));
        if (esperado == 201) {
            resultado
                .andExpect(jsonPath("$.etapa").value("PROPUESTA"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.estado").value("ENVIADA"))
                .andExpect(jsonPath("$.fechaEntrega").value(AHORA.toString()))
                .andExpect(jsonPath("$.autor.id").value(estudiante.getId().intValue()));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "admin,ROLE_ADMIN,200",
        "estudiante,ROLE_ESTUDIANTE,403",
        "tutor,ROLE_TUTOR,200",
        "jurado,ROLE_JURADO,403",
        "user,ROLE_USER,403",
        "otro-tutor,ROLE_TUTOR,403",
    })
    void revisarEntregaSegunRolYRelacion(String login, String rol, int esperado) throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.ENVIADA);
        como(login, rol, json(put("/api/entregas/{id}/revision", entrega.getId()), Map.of("estado", "EN_REVISION"))).andExpect(
            status().is(esperado)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "admin,ROLE_ADMIN,201",
        "estudiante,ROLE_ESTUDIANTE,403",
        "tutor,ROLE_TUTOR,201",
        "jurado,ROLE_JURADO,403",
        "user,ROLE_USER,403",
        "otro-tutor,ROLE_TUTOR,403",
    })
    void crearObservacionSegunRolYRelacion(String login, String rol, int esperado) throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.ENVIADA);
        var cuerpo = Map.of(
            "contenido",
            "Corregir citas",
            "tipo",
            "CORRECCION_REQUERIDA",
            "entrega",
            Map.of("id", entrega.getId()),
            "resuelta",
            true,
            "fechaCreacion",
            Instant.EPOCH.toString(),
            "autor",
            Map.of("id", estudiante.getId())
        );
        var resultado = como(login, rol, json(post("/api/observacions"), cuerpo)).andExpect(status().is(esperado));
        if (esperado == 201) {
            resultado
                .andExpect(jsonPath("$.resuelta").value(false))
                .andExpect(jsonPath("$.fechaCreacion").value(AHORA.toString()))
                .andExpect(jsonPath("$.autor.login").value(PREFIJO + login));
            assertThat(entregas.findById(entrega.getId()).orElseThrow().getEstado()).isEqualTo(EstadoEntrega.CON_OBSERVACIONES);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "admin,ROLE_ADMIN,200",
        "estudiante,ROLE_ESTUDIANTE,403",
        "tutor,ROLE_TUTOR,200",
        "jurado,ROLE_JURADO,403",
        "user,ROLE_USER,403",
        "otro-tutor,ROLE_TUTOR,403",
    })
    void resolverObservacionSegunRolYRelacion(String login, String rol, int esperado) throws Exception {
        Observacion observacion = crearObservacion(crearEntrega(proyecto, EstadoEntrega.CON_OBSERVACIONES));
        como(login, rol, put("/api/observacions/{id}/resolver", observacion.getId())).andExpect(status().is(esperado));
        assertThat(observaciones.findById(observacion.getId()).orElseThrow().getResuelta()).isEqualTo(esperado == 200);
    }

    @ParameterizedTest
    @CsvSource({
        "admin,ROLE_ADMIN,403",
        "estudiante,ROLE_ESTUDIANTE,403",
        "tutor,ROLE_TUTOR,403",
        "jurado,ROLE_JURADO,201",
        "user,ROLE_USER,403",
        "otro-jurado,ROLE_JURADO,403",
    })
    void evaluarSegunRolYRelacion(String login, String rol, int esperado) throws Exception {
        proyecto.setEtapaActual(Etapa.ANTEPROYECTO);
        proyectos.saveAndFlush(proyecto);
        var resultado = como(login, rol, json(post("/api/evaluacions"), cuerpoEvaluacion())).andExpect(status().is(esperado));
        if (esperado == 201) {
            resultado
                .andExpect(jsonPath("$.etapa").value("ANTEPROYECTO"))
                .andExpect(jsonPath("$.jurado.id").value(jurado.getId().intValue()))
                .andExpect(jsonPath("$.fechaEvaluacion").value(AHORA.toString()));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "admin,ROLE_ADMIN,201",
        "estudiante,ROLE_ESTUDIANTE,403",
        "tutor,ROLE_TUTOR,403",
        "jurado,ROLE_JURADO,403",
        "user,ROLE_USER,403",
    })
    void soloAdminFijaFechas(String login, String rol, int esperado) throws Exception {
        como(
            login,
            rol,
            json(
                post("/api/fecha-limite-etapas"),
                Map.of(
                    "proyecto",
                    Map.of("id", proyecto.getId()),
                    "etapa",
                    "SUSTENTACION",
                    "fechaLimite",
                    AHORA.plusSeconds(3600).toString()
                )
            )
        ).andExpect(status().is(esperado));
    }

    @ParameterizedTest
    @CsvSource({
        "admin,ROLE_ADMIN,204",
        "estudiante,ROLE_ESTUDIANTE,403",
        "tutor,ROLE_TUTOR,403",
        "jurado,ROLE_JURADO,403",
        "user,ROLE_USER,403",
    })
    void soloAdminBorra(String login, String rol, int esperado) throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.ENVIADA);
        Evaluacion evaluacion = evaluaciones.saveAndFlush(
            new Evaluacion()
                .proyecto(proyecto)
                .jurado(jurado)
                .etapa(Etapa.ANTEPROYECTO)
                .calificacion(BigDecimal.ONE)
                .concepto(Concepto.REPROBADO)
                .fechaEvaluacion(Instant.EPOCH)
        );
        FechaLimiteEtapa fecha = fechas.saveAndFlush(new FechaLimiteEtapa().proyecto(proyecto).etapa(Etapa.PROPUESTA).fechaLimite(AHORA));
        Observacion observacion = crearObservacion(entrega);
        for (String ruta : List.of(
            "/api/observacions/" + observacion.getId(),
            "/api/entregas/" + entrega.getId(),
            "/api/evaluacions/" + evaluacion.getId(),
            "/api/fecha-limite-etapas/" + fecha.getId(),
            "/api/proyecto-grados/" + ajeno.getId()
        )) {
            como(login, rol, delete(ruta)).andExpect(status().is(esperado));
        }
    }

    @ParameterizedTest
    @CsvSource({ "admin,ROLE_ADMIN", "estudiante,ROLE_ESTUDIANTE", "tutor,ROLE_TUTOR", "jurado,ROLE_JURADO", "user,ROLE_USER" })
    void historialNuncaSeEscribePorRest(String login, String rol) throws Exception {
        HistorialEtapa historial = historiales.saveAndFlush(
            new HistorialEtapa().proyecto(proyecto).etapaDestino(Etapa.PROPUESTA).fecha(AHORA)
        );
        var cuerpo = Map.of(
            "id",
            historial.getId(),
            "proyecto",
            Map.of("id", proyecto.getId()),
            "etapaDestino",
            "PROPUESTA",
            "fecha",
            AHORA.toString()
        );
        var creacion = new LinkedHashMap<>(cuerpo);
        creacion.remove("id");
        como(login, rol, json(post("/api/historial-etapas"), creacion)).andExpect(status().isForbidden());
        como(login, rol, json(put("/api/historial-etapas/{id}", historial.getId()), cuerpo)).andExpect(status().isForbidden());
        como(login, rol, json(patch("/api/historial-etapas/{id}", historial.getId()), cuerpo)).andExpect(status().isForbidden());
        como(login, rol, delete("/api/historial-etapas/{id}", historial.getId())).andExpect(status().isForbidden());
        assertThat(historiales.existsById(historial.getId())).isTrue();
    }

    private Map<String, Long> entidadesVisibles(ProyectoGrado p) {
        Entrega entrega = crearEntrega(p, EstadoEntrega.CON_OBSERVACIONES);
        Observacion observacion = crearObservacion(entrega);
        Evaluacion evaluacion = evaluaciones.saveAndFlush(
            new Evaluacion()
                .proyecto(p)
                .jurado(jurado)
                .etapa(Etapa.ANTEPROYECTO)
                .calificacion(BigDecimal.ONE)
                .concepto(Concepto.REPROBADO)
                .fechaEvaluacion(Instant.EPOCH)
        );
        FechaLimiteEtapa fecha = fechas.saveAndFlush(new FechaLimiteEtapa().proyecto(p).etapa(Etapa.PROPUESTA).fechaLimite(AHORA));
        HistorialEtapa historial = historiales.saveAndFlush(new HistorialEtapa().proyecto(p).etapaDestino(Etapa.PROPUESTA).fecha(AHORA));
        return Map.of(
            "proyecto-grados",
            p.getId(),
            "entregas",
            entrega.getId(),
            "observacions",
            observacion.getId(),
            "evaluacions",
            evaluacion.getId(),
            "fecha-limite-etapas",
            fecha.getId(),
            "historial-etapas",
            historial.getId()
        );
    }

    @ParameterizedTest
    @CsvSource({ "estudiante,ROLE_ESTUDIANTE", "tutor,ROLE_TUTOR", "jurado,ROLE_JURADO" })
    void listadosYGetSoloMuestranLosProyectosRelacionados(String login, String rol) throws Exception {
        proyecto.addEstudiante(usuarios.findOneByLogin(PREFIJO + "otro-estudiante").orElseThrow());
        proyecto.addJurado(usuarios.findOneByLogin(PREFIJO + "otro-jurado").orElseThrow());
        proyectos.saveAndFlush(proyecto);
        Map<String, Long> propios = entidadesVisibles(proyecto);
        Map<String, Long> otros = entidadesVisibles(ajeno);
        for (String entidad : propios.keySet()) {
            String ruta = "/api/" + entidad;
            for (boolean eager : List.of(true, false)) {
                como(login, rol, get(ruta).param("eagerload", Boolean.toString(eager)).param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id", hasItem(propios.get(entidad).intValue())))
                    .andExpect(jsonPath("$[*].id", not(hasItem(otros.get(entidad).intValue()))));
            }
            como(login, rol, get(ruta + "/{id}", propios.get(entidad))).andExpect(status().isOk());
            como(login, rol, get(ruta + "/{id}", otros.get(entidad))).andExpect(status().isForbidden());
        }
        for (String entidad : List.of("proyecto-grados", "entregas")) {
            String ruta = "/api/" + entidad;
            como(login, rol, get(ruta).param("id.equals", otros.get(entidad).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
            como(login, rol, get(ruta + "/count").param("id.in", propios.get(entidad) + "," + otros.get(entidad)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
            como(
                login,
                rol,
                get(ruta)
                    .param("id.in", propios.get(entidad) + "," + otros.get(entidad))
                    .param("page", "0")
                    .param("size", "1")
            )
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(propios.get(entidad).intValue()));
            como(login, rol, get(ruta + "/count").param("id.equals", otros.get(entidad).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
        }
    }

    @Test
    void adminVeTodoYUsuarioSinRelacionNoVeNada() throws Exception {
        Map<String, Long> propios = entidadesVisibles(proyecto);
        Map<String, Long> otros = entidadesVisibles(ajeno);
        for (String entidad : propios.keySet()) {
            String ruta = "/api/" + entidad;
            como("admin", ADMIN, get(ruta).param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItems(propios.get(entidad).intValue(), otros.get(entidad).intValue())));
            como("admin", ADMIN, get(ruta + "/{id}", otros.get(entidad))).andExpect(status().isOk());
            como("user", USER, get(ruta)).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
            como("user", USER, get(ruta + "/{id}", propios.get(entidad))).andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "put", "patch" })
    void estudianteEditaSoloTituloYResumenYAdminProtegeWorkflow(String metodo) throws Exception {
        var cuerpo = new LinkedHashMap<>(cuerpoProyecto());
        cuerpo.put("id", proyecto.getId());
        cuerpo.put("programa", "Programa manipulado");
        cuerpo.put("estudiantes", List.of());
        cuerpo.put("jurados", List.of());
        cuerpo.put("tutor", Map.of("id", jurado.getId()));
        var peticion = metodo.equals("put")
            ? put("/api/proyecto-grados/{id}", proyecto.getId())
            : patch("/api/proyecto-grados/{id}", proyecto.getId());
        como("estudiante", ESTUDIANTE, json(peticion, cuerpo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Propuesta registrada"))
            .andExpect(jsonPath("$.resumen").value("Resumen"))
            .andExpect(jsonPath("$.programa").value("Ingeniería"))
            .andExpect(jsonPath("$.etapaActual").value("PROPUESTA"))
            .andExpect(jsonPath("$.estado").value("ACTIVO"))
            .andExpect(jsonPath("$.fechaRegistro").value(Instant.EPOCH.toString()))
            .andExpect(jsonPath("$.tutor.id").value(tutor.getId().intValue()))
            .andExpect(jsonPath("$.estudiantes[0].id").value(estudiante.getId().intValue()));
        peticion = metodo.equals("put")
            ? put("/api/proyecto-grados/{id}", proyecto.getId())
            : patch("/api/proyecto-grados/{id}", proyecto.getId());
        como("admin", ADMIN, json(peticion, cuerpo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.etapaActual").value("PROPUESTA"))
            .andExpect(jsonPath("$.estado").value("ACTIVO"))
            .andExpect(jsonPath("$.fechaRegistro").value(Instant.EPOCH.toString()))
            .andExpect(jsonPath("$.porcentajeAvance", lessThan(100)))
            .andExpect(jsonPath("$.programa").value("Programa manipulado"))
            .andExpect(jsonPath("$.tutor.id").value(jurado.getId().intValue()));
    }

    @ParameterizedTest
    @CsvSource({ "tutor,ROLE_TUTOR", "jurado,ROLE_JURADO", "otro-estudiante,ROLE_ESTUDIANTE" })
    void noPuedenEditarElProyecto(String login, String rol) throws Exception {
        como(
            login,
            rol,
            json(patch("/api/proyecto-grados/{id}", proyecto.getId()), Map.of("id", proyecto.getId(), "titulo", "Título editado"))
        ).andExpect(status().isForbidden());
    }

    @Test
    void estudianteNoEditaFueraDePropuestaNiEntregaYaRevisada() throws Exception {
        proyecto.setEtapaActual(Etapa.ANTEPROYECTO);
        proyectos.saveAndFlush(proyecto);
        como(
            "estudiante",
            ESTUDIANTE,
            json(patch("/api/proyecto-grados/{id}", proyecto.getId()), Map.of("id", proyecto.getId(), "titulo", "Título editado"))
        ).andExpect(status().isForbidden());
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.EN_REVISION);
        como(
            "estudiante",
            ESTUDIANTE,
            json(patch("/api/entregas/{id}", entrega.getId()), Map.of("id", entrega.getId(), "titulo", "Título editado"))
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = { "put", "patch" })
    void entregaEnviadaSoloPermiteCambiarContenido(String metodo) throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.ENVIADA);
        var cuerpo = new LinkedHashMap<>(cuerpoEntrega());
        cuerpo.put("id", entrega.getId());
        cuerpo.put("proyecto", Map.of("id", ajeno.getId()));
        var peticion = metodo.equals("put") ? put("/api/entregas/{id}", entrega.getId()) : patch("/api/entregas/{id}", entrega.getId());
        como("estudiante", ESTUDIANTE, json(peticion, cuerpo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Documento enviado"))
            .andExpect(jsonPath("$.estado").value("ENVIADA"))
            .andExpect(jsonPath("$.version").value(1))
            .andExpect(jsonPath("$.etapa").value("PROPUESTA"))
            .andExpect(jsonPath("$.fechaEntrega").value(Instant.EPOCH.toString()))
            .andExpect(jsonPath("$.proyecto.id").value(proyecto.getId().intValue()));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoProyecto.class, names = "ACTIVO", mode = EnumSource.Mode.EXCLUDE)
    void proyectoInactivoEsConflicto(EstadoProyecto estado) throws Exception {
        proyecto.setEstado(estado);
        proyectos.saveAndFlush(proyecto);
        conflicto(como("estudiante", ESTUDIANTE, json(post("/api/entregas"), cuerpoEntrega())), "PROYECTO_NO_ACTIVO");
    }

    @Test
    void fechaLimiteVencidaEsConflicto() throws Exception {
        fechas.saveAndFlush(new FechaLimiteEtapa().proyecto(proyecto).etapa(Etapa.PROPUESTA).fechaLimite(AHORA.minusSeconds(1)));
        conflicto(como("estudiante", ESTUDIANTE, json(post("/api/entregas"), cuerpoEntrega())), "FECHA_LIMITE_VENCIDA");
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEntrega.class, names = { "ENVIADA", "EN_REVISION" })
    void entregaEnCursoEsConflicto(EstadoEntrega estado) throws Exception {
        crearEntrega(proyecto, estado);
        conflicto(como("estudiante", ESTUDIANTE, json(post("/api/entregas"), cuerpoEntrega())), "ENTREGA_EN_CURSO");
    }

    @Test
    void etapaYaAprobadaEsConflicto() throws Exception {
        crearEntrega(proyecto, EstadoEntrega.APROBADA);
        conflicto(como("estudiante", ESTUDIANTE, json(post("/api/entregas"), cuerpoEntrega())), "ETAPA_YA_APROBADA");
    }

    @Test
    void correccionesPendientesImpidenAprobarHastaResolverlas() throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.CON_OBSERVACIONES);
        Observacion observacion = crearObservacion(entrega);
        conflicto(
            como("tutor", TUTOR, json(put("/api/entregas/{id}/revision", entrega.getId()), Map.of("estado", "APROBADA"))),
            "CORRECCIONES_PENDIENTES"
        );
        como("tutor", TUTOR, put("/api/observacions/{id}/resolver", observacion.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resuelta").value(true));
        como("tutor", TUTOR, json(put("/api/entregas/{id}/revision", entrega.getId()), Map.of("estado", "APROBADA")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    @Test
    void noSeRevisanEntregasDeOtraEtapa() throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.ENVIADA);
        proyecto.setEtapaActual(Etapa.ANTEPROYECTO);
        proyectos.saveAndFlush(proyecto);
        como("admin", ADMIN, json(put("/api/entregas/{id}/revision", entrega.getId()), Map.of("estado", "EN_REVISION"))).andExpect(
            status().isForbidden()
        );
    }

    @Test
    void estadoDeRevisionInvalidoEsBadRequest() throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.ENVIADA);
        como("tutor", TUTOR, json(put("/api/entregas/{id}/revision", entrega.getId()), Map.of("estado", "ENVIADA"))).andExpect(
            status().isBadRequest()
        );
        como("tutor", TUTOR, json(put("/api/entregas/{id}/revision", entrega.getId()), Map.of())).andExpect(status().isBadRequest());
    }

    @Test
    void etapaNoEvaluableEsConflicto() throws Exception {
        conflicto(como("jurado", JURADO, json(post("/api/evaluacions"), cuerpoEvaluacion())), "ETAPA_NO_EVALUABLE");
    }

    @Test
    void evaluacionDuplicadaEsConflicto() throws Exception {
        proyecto.setEtapaActual(Etapa.ANTEPROYECTO);
        proyectos.saveAndFlush(proyecto);
        evaluaciones.saveAndFlush(
            new Evaluacion()
                .proyecto(proyecto)
                .jurado(jurado)
                .etapa(Etapa.ANTEPROYECTO)
                .calificacion(BigDecimal.ONE)
                .concepto(Concepto.REPROBADO)
                .fechaEvaluacion(Instant.EPOCH)
        );
        conflicto(como("jurado", JURADO, json(post("/api/evaluacions"), cuerpoEvaluacion())), "EVALUACION_DUPLICADA");
    }

    @ParameterizedTest
    @CsvSource({ "2.9,APROBADO", "2.9,APROBADO_CON_MODIFICACIONES", "3.0,REPROBADO" })
    void conceptoIncoherenteEsConflicto(BigDecimal nota, Concepto concepto) throws Exception {
        proyecto.setEtapaActual(Etapa.SUSTENTACION);
        proyectos.saveAndFlush(proyecto);
        var cuerpo = new LinkedHashMap<>(cuerpoEvaluacion());
        cuerpo.put("calificacion", nota);
        cuerpo.put("concepto", concepto);
        conflicto(como("jurado", JURADO, json(post("/api/evaluacions"), cuerpo)), "CONCEPTO_INCOHERENTE");
    }

    @ParameterizedTest
    @CsvSource({ "admin,ROLE_ADMIN,200", "estudiante,ROLE_ESTUDIANTE,403", "tutor,ROLE_TUTOR,403", "jurado,ROLE_JURADO,403" })
    void soloAdminEditaEvaluacion(String login, String rol, int esperado) throws Exception {
        Evaluacion evaluacion = evaluaciones.saveAndFlush(
            new Evaluacion()
                .proyecto(proyecto)
                .jurado(jurado)
                .etapa(Etapa.ANTEPROYECTO)
                .calificacion(BigDecimal.ONE)
                .concepto(Concepto.REPROBADO)
                .fechaEvaluacion(Instant.EPOCH)
        );
        como(
            login,
            rol,
            json(
                patch("/api/evaluacions/{id}", evaluacion.getId()),
                Map.of("id", evaluacion.getId(), "calificacion", 4, "concepto", "APROBADO")
            )
        ).andExpect(status().is(esperado));
    }

    @Test
    void directorioDevuelveSoloUsuariosActivosDelRolSinDatosPrivados() throws Exception {
        crearUsuario("tutor-inactivo", TUTOR, false);
        como("admin", ADMIN, get("/api/usuarios/por-rol/{authority}", TUTOR))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].login", hasItem(tutor.getLogin())))
            .andExpect(jsonPath("$[*].login", not(hasItem(PREFIJO + "tutor-inactivo"))))
            .andExpect(jsonPath("$[*].login", not(hasItem(estudiante.getLogin()))))
            .andExpect(jsonPath("$[0]", aMapWithSize(4)))
            .andExpect(jsonPath("$[0].firstName").exists())
            .andExpect(jsonPath("$[0].lastName").exists())
            .andExpect(jsonPath("$[0].password").doesNotExist())
            .andExpect(jsonPath("$[0].email").doesNotExist());
        como("admin", ADMIN, get("/api/usuarios/por-rol/ROLE_INEXISTENTE")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @ParameterizedTest
    @CsvSource({ "estudiante,ROLE_ESTUDIANTE", "tutor,ROLE_TUTOR", "jurado,ROLE_JURADO", "user,ROLE_USER" })
    void directorioEsPrivadoParaAdmin(String login, String rol) throws Exception {
        como(login, rol, get("/api/usuarios/por-rol/{authority}", TUTOR)).andExpect(status().isForbidden());
    }

    @Test
    void estudianteConsultaObservacionesDeSuEntregaEnOrdenCronologico() throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.CON_OBSERVACIONES);
        Observacion segunda = crearObservacion(entrega);
        segunda.setFechaCreacion(AHORA);
        observaciones.saveAndFlush(segunda);
        Observacion primera = crearObservacion(entrega);
        crearObservacion(crearEntrega(ajeno, EstadoEntrega.CON_OBSERVACIONES));
        como("estudiante", ESTUDIANTE, get("/api/entregas/{id}/observacions", entrega.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(primera.getId().intValue()))
            .andExpect(jsonPath("$[1].id").value(segunda.getId().intValue()));
    }

    @Test
    void juradoNoAsignadoNoConsultaObservacionesDeLaEntrega() throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.CON_OBSERVACIONES);
        crearObservacion(entrega);
        como("otro-jurado", JURADO, get("/api/entregas/{id}/observacions", entrega.getId())).andExpect(status().isForbidden());
    }

    @Test
    void consultaObservacionesDevuelveListaVaciaO404SegunExistaLaEntrega() throws Exception {
        Entrega entrega = crearEntrega(proyecto, EstadoEntrega.ENVIADA);
        como("estudiante", ESTUDIANTE, get("/api/entregas/{id}/observacions", entrega.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
        como("estudiante", ESTUDIANTE, get("/api/entregas/{id}/observacions", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }
}
