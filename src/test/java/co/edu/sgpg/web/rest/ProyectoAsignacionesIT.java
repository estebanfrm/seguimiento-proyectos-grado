package co.edu.sgpg.web.rest;

import static co.edu.sgpg.security.AuthoritiesConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.Authority;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.service.dto.AsignacionesDTO;
import jakarta.persistence.EntityManager;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(authorities = ADMIN)
@Import(ProyectoAsignacionesIT.RelojDePrueba.class)
class ProyectoAsignacionesIT {

    private static final Instant AHORA = Instant.parse("2026-11-15T12:00:00Z");
    private static final String URL = "/api/proyecto-grados/{id}/asignaciones";

    @TestConfiguration(proxyBeanMethods = false)
    static class RelojDePrueba {

        @Bean
        @Primary
        Clock relojAsignaciones() {
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

    private ProyectoGrado proyecto;
    private User tutor;
    private User jurado;
    private User estudiante;

    @BeforeEach
    void preparar() {
        tutor = crearUsuario("tutor", TUTOR, true);
        jurado = crearUsuario("jurado", JURADO, true);
        estudiante = crearUsuario("estudiante", ESTUDIANTE, true);
        proyecto = proyectos.saveAndFlush(
            new ProyectoGrado()
                .titulo("Proyecto de asignaciones")
                .resumen("Resumen académico")
                .programa("Ingeniería")
                .etapaActual(Etapa.PROPUESTA)
                .estado(EstadoProyecto.ACTIVO)
                .porcentajeAvance(12)
                .fechaRegistro(Instant.EPOCH)
                .fechaActualizacion(Instant.EPOCH)
                .tutor(tutor)
                .addJurado(jurado)
                .addEstudiante(estudiante)
        );
    }

    @Test
    void tutorNuloLoRetiraYRecalculaAvanceSinCambiarElWorkflow() throws Exception {
        asignar(new AsignacionesDTO(null, List.of(jurado.getId()), List.of(estudiante.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tutor").doesNotExist())
            .andExpect(jsonPath("$.porcentajeAvance").value(8))
            .andExpect(jsonPath("$.fechaActualizacion").value(AHORA.toString()))
            .andExpect(jsonPath("$.fechaRegistro").value(Instant.EPOCH.toString()))
            .andExpect(jsonPath("$.etapaActual").value("PROPUESTA"))
            .andExpect(jsonPath("$.estado").value("ACTIVO"))
            .andExpect(jsonPath("$.titulo").value("Proyecto de asignaciones"));

        var guardado = recargar();
        assertThat(guardado.getTutor()).isNull();
        assertThat(guardado.getPorcentajeAvance()).isEqualTo(8);
        assertThat(guardado.getFechaActualizacion()).isEqualTo(AHORA);
        mvc.perform(get("/api/proyecto-grados/{id}/workflow", proyecto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requisitos[?(@.codigo == 'TUTOR_ASIGNADO')].cumplido", contains(false)));
    }

    @Test
    void reemplazaJuradosYRecalculaElRequisitoDeAnteproyecto() throws Exception {
        User primero = crearUsuario("jurado-nuevo-1", JURADO, true);
        User segundo = crearUsuario("jurado-nuevo-2", JURADO, true);
        proyecto.setEtapaActual(Etapa.ANTEPROYECTO);
        proyecto.setPorcentajeAvance(23);
        proyectos.saveAndFlush(proyecto);

        asignar(new AsignacionesDTO(tutor.getId(), List.of(primero.getId(), segundo.getId()), List.of(estudiante.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jurados[*].id", containsInAnyOrder(primero.getId().intValue(), segundo.getId().intValue())))
            .andExpect(jsonPath("$.porcentajeAvance").value(26));

        var guardado = recargar();
        assertThat(guardado.getJurados()).extracting(User::getId).containsExactlyInAnyOrder(primero.getId(), segundo.getId());
        assertThat(guardado.getPorcentajeAvance()).isEqualTo(26);
    }

    @Test
    void reemplazaTutorYEstudiantesConUsuariosDelRolCorrecto() throws Exception {
        User otroTutor = crearUsuario("otro-tutor", TUTOR, true);
        User otroEstudiante = crearUsuario("otro-estudiante", ESTUDIANTE, true);
        asignar(new AsignacionesDTO(otroTutor.getId(), List.of(), List.of(otroEstudiante.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tutor.id").value(otroTutor.getId().intValue()))
            .andExpect(jsonPath("$.estudiantes[*].id", contains(otroEstudiante.getId().intValue())))
            .andExpect(jsonPath("$.jurados").isEmpty())
            .andExpect(jsonPath("$.porcentajeAvance").value(12));
        var guardado = recargar();
        assertThat(guardado.getTutor().getId()).isEqualTo(otroTutor.getId());
        assertThat(guardado.getEstudiantes()).extracting(User::getId).containsExactly(otroEstudiante.getId());
    }

    @Test
    void listasVaciasEliminanLasAsignacionesYRecalculanElAvance() throws Exception {
        asignar(new AsignacionesDTO(null, List.of(), List.of()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tutor").doesNotExist())
            .andExpect(jsonPath("$.jurados").isEmpty())
            .andExpect(jsonPath("$.estudiantes").isEmpty())
            .andExpect(jsonPath("$.porcentajeAvance").value(4));
        var guardado = recargar();
        assertThat(guardado.getTutor()).isNull();
        assertThat(guardado.getJurados()).isEmpty();
        assertThat(guardado.getEstudiantes()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "ROLE_TUTOR,AUSENTE",
        "ROLE_TUTOR,INACTIVO",
        "ROLE_TUTOR,ROL_INCORRECTO",
        "ROLE_JURADO,AUSENTE",
        "ROLE_JURADO,INACTIVO",
        "ROLE_JURADO,ROL_INCORRECTO",
        "ROLE_ESTUDIANTE,AUSENTE",
        "ROLE_ESTUDIANTE,INACTIVO",
        "ROLE_ESTUDIANTE,ROL_INCORRECTO",
    })
    void usuarioInvalidoDevuelve409SinModificarAsignaciones(String rol, String defecto) throws Exception {
        Long id = defecto.equals("AUSENTE")
            ? Long.MAX_VALUE
            : crearUsuario("invalido", defecto.equals("ROL_INCORRECTO") ? USER : rol, !defecto.equals("INACTIVO")).getId();
        asignar(
            new AsignacionesDTO(
                rol.equals(TUTOR) ? id : null,
                rol.equals(JURADO) ? List.of(id) : List.of(),
                rol.equals(ESTUDIANTE) ? List.of(id) : List.of()
            )
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("USUARIO_ROL_INVALIDO"));
        var guardado = recargar();
        assertThat(guardado.getTutor().getId()).isEqualTo(tutor.getId());
        assertThat(guardado.getJurados()).extracting(User::getId).containsExactly(jurado.getId());
        assertThat(guardado.getEstudiantes()).extracting(User::getId).containsExactly(estudiante.getId());
        assertThat(guardado.getFechaActualizacion()).isEqualTo(Instant.EPOCH);
    }

    @ParameterizedTest
    @ValueSource(strings = { ESTUDIANTE, TUTOR, JURADO, USER })
    void noAdminDevuelve403(String rol) throws Exception {
        mvc.perform(
            put(URL, proyecto.getId())
                .with(user("sin-permiso").authorities(new SimpleGrantedAuthority(rol)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(new AsignacionesDTO(null, List.of(), List.of())))
        ).andExpect(status().isForbidden());
        assertThat(recargar().getTutor().getId()).isEqualTo(tutor.getId());
    }

    @Test
    void proyectoInexistenteDevuelve404() throws Exception {
        mvc.perform(
            put(URL, Long.MAX_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(new AsignacionesDTO(null, List.of(), List.of())))
        ).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvSource({ "juradoIds,false", "juradoIds,true", "estudianteIds,false", "estudianteIds,true" })
    void listasObligatoriasNoAdmitenOmisionNiNull(String campo, boolean nula) throws Exception {
        var cuerpo = new LinkedHashMap<String, Object>(Map.of("juradoIds", List.of(), "estudianteIds", List.of()));
        if (nula) cuerpo.put(campo, null);
        else cuerpo.remove(campo);
        mvc.perform(put(URL, proyecto.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cuerpo))).andExpect(
            status().isBadRequest()
        );
        assertThat(recargar().getTutor().getId()).isEqualTo(tutor.getId());
    }

    private ResultActions asignar(AsignacionesDTO asignaciones) throws Exception {
        return mvc.perform(put(URL, proyecto.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(asignaciones)));
    }

    private ProyectoGrado recargar() {
        em.flush();
        em.clear();
        return proyectos.findById(proyecto.getId()).orElseThrow();
    }

    private User crearUsuario(String login, String rol, boolean activo) {
        Authority authority = em.find(Authority.class, rol);
        if (authority == null) {
            authority = new Authority().name(rol);
            em.persist(authority);
        }
        User usuario = new User();
        usuario.setLogin("asignaciones-" + login);
        usuario.setPassword("x".repeat(60));
        usuario.setEmail("asignaciones-" + login + "@example.test");
        usuario.setActivated(activo);
        usuario.setAuthorities(Set.of(authority));
        return usuarios.saveAndFlush(usuario);
    }
}
