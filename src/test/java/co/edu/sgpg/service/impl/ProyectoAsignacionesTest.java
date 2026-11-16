package co.edu.sgpg.service.impl;

import static co.edu.sgpg.security.AuthoritiesConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.Authority;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.service.AvanceService;
import co.edu.sgpg.service.PermisosEntidadService;
import co.edu.sgpg.service.ProyectoAccessService;
import co.edu.sgpg.service.ReglaNegocioException;
import co.edu.sgpg.service.VisibilidadProyectoService;
import co.edu.sgpg.service.dto.AsignacionesDTO;
import co.edu.sgpg.service.mapper.ProyectoGradoMapperImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProyectoAsignacionesTest {

    private static final Instant AHORA = Instant.parse("2026-11-15T12:00:00Z");

    @Mock
    private ProyectoGradoRepository proyectos;

    @Mock
    private UserRepository usuarios;

    @Mock
    private ProyectoAccessService acceso;

    @Mock
    private PermisosEntidadService permisos;

    @Mock
    private VisibilidadProyectoService visibilidad;

    @Mock
    private AvanceService avance;

    private ProyectoGradoServiceImpl service;
    private ProyectoGrado proyecto;
    private User tutorAnterior;
    private User juradoAnterior;
    private User estudianteAnterior;

    @BeforeEach
    void preparar() {
        service = new ProyectoGradoServiceImpl(
            proyectos,
            new ProyectoGradoMapperImpl(),
            acceso,
            permisos,
            visibilidad,
            avance,
            Clock.fixed(AHORA, ZoneOffset.UTC),
            usuarios
        );
        tutorAnterior = usuario(2L, TUTOR);
        juradoAnterior = usuario(3L, JURADO);
        estudianteAnterior = usuario(4L, ESTUDIANTE);
        proyecto = new ProyectoGrado()
            .id(1L)
            .titulo("Proyecto académico")
            .resumen("Resumen original")
            .programa("Ingeniería")
            .etapaActual(Etapa.PROPUESTA)
            .estado(EstadoProyecto.ACTIVO)
            .porcentajeAvance(8)
            .fechaRegistro(Instant.EPOCH)
            .fechaActualizacion(Instant.EPOCH)
            .tutor(tutorAnterior)
            .addJurado(juradoAnterior)
            .addEstudiante(estudianteAnterior);
    }

    @Test
    void tutorNuloRetiraElAnteriorYListasVaciasEliminanLasColecciones() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(proyectos.save(proyecto)).thenReturn(proyecto);
        when(avance.recalcular(1L)).thenAnswer(invocation -> {
            proyecto.setPorcentajeAvance(0);
            return 0;
        });

        var resultado = service.actualizarAsignaciones(1L, new AsignacionesDTO(null, List.of(), List.of()));

        assertThat(proyecto.getTutor()).isNull();
        assertThat(proyecto.getJurados()).isEmpty();
        assertThat(proyecto.getEstudiantes()).isEmpty();
        assertThat(resultado.getTutor()).isNull();
        assertThat(resultado.getPorcentajeAvance()).isZero();
        assertThat(resultado.getFechaActualizacion()).isEqualTo(AHORA);
        assertThat(resultado.getFechaRegistro()).isEqualTo(Instant.EPOCH);
        assertThat(resultado.getEtapaActual()).isEqualTo(Etapa.PROPUESTA);
        assertThat(resultado.getEstado()).isEqualTo(EstadoProyecto.ACTIVO);
        assertThat(resultado.getTitulo()).isEqualTo("Proyecto académico");
        assertThat(resultado.getResumen()).isEqualTo("Resumen original");
        assertThat(resultado.getPrograma()).isEqualTo("Ingeniería");
        var orden = inOrder(permisos, proyectos, avance);
        orden.verify(permisos).admin();
        orden.verify(proyectos).findById(1L);
        orden.verify(proyectos).save(proyecto);
        orden.verify(avance).recalcular(1L);
        verifyNoInteractions(usuarios);
    }

    @Test
    void reemplazaTodasLasAsignacionesYDeduplicaIdentificadores() {
        User tutor = usuario(12L, TUTOR);
        User jurado = usuario(13L, JURADO);
        User estudiante = usuario(14L, ESTUDIANTE);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(proyectos.save(proyecto)).thenReturn(proyecto);
        when(usuarios.findById(12L)).thenReturn(Optional.of(tutor));
        when(usuarios.findById(13L)).thenReturn(Optional.of(jurado));
        when(usuarios.findById(14L)).thenReturn(Optional.of(estudiante));

        var resultado = service.actualizarAsignaciones(1L, new AsignacionesDTO(12L, List.of(13L, 13L), List.of(14L)));

        assertThat(proyecto.getTutor()).isSameAs(tutor);
        assertThat(proyecto.getJurados()).containsExactly(jurado);
        assertThat(proyecto.getEstudiantes()).containsExactly(estudiante);
        assertThat(resultado.getTutor().getId()).isEqualTo(12L);
        assertThat(resultado.getJurados()).extracting("id").containsExactly(13L);
        assertThat(resultado.getEstudiantes()).extracting("id").containsExactly(14L);
        assertThat(proyecto.getFechaActualizacion()).isEqualTo(AHORA);
        verify(avance).recalcular(1L);
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
    void rechazaUsuarioInexistenteInactivoOSinElRolEsperado(String rol, String defecto) {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        User candidato = usuario(99L, defecto.equals("ROL_INCORRECTO") ? USER : rol);
        candidato.setActivated(!defecto.equals("INACTIVO"));
        when(usuarios.findById(99L)).thenReturn(defecto.equals("AUSENTE") ? Optional.empty() : Optional.of(candidato));
        var dto = new AsignacionesDTO(
            rol.equals(TUTOR) ? 99L : null,
            rol.equals(JURADO) ? List.of(99L) : List.of(),
            rol.equals(ESTUDIANTE) ? List.of(99L) : List.of()
        );

        assertThatThrownBy(() -> service.actualizarAsignaciones(1L, dto)).isInstanceOfSatisfying(ReglaNegocioException.class, error -> {
            assertThat(error.getCodigo()).isEqualTo("USUARIO_ROL_INVALIDO");
            assertThat(error.getBody().getDetail()).contains("99", rol);
        });
        comprobarSinCambios();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void identificadorNuloDentroDeUnaListaEsUsuarioInvalido(boolean jurado) {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        List<Long> nulos = Arrays.asList((Long) null);
        var dto = new AsignacionesDTO(null, jurado ? nulos : List.of(), jurado ? List.of() : nulos);

        assertThatThrownBy(() -> service.actualizarAsignaciones(1L, dto)).isInstanceOfSatisfying(ReglaNegocioException.class, error ->
            assertThat(error.getCodigo()).isEqualTo("USUARIO_ROL_INVALIDO")
        );
        comprobarSinCambios();
        verifyNoInteractions(usuarios);
    }

    @Test
    void unEstudianteInvalidoNoAplicaLasOtrasAsignacionesValidas() {
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(usuarios.findById(12L)).thenReturn(Optional.of(usuario(12L, TUTOR)));
        when(usuarios.findById(13L)).thenReturn(Optional.of(usuario(13L, JURADO)));
        var dto = new AsignacionesDTO(12L, List.of(13L), List.of(99L));

        assertThatThrownBy(() -> service.actualizarAsignaciones(1L, dto)).isInstanceOf(ReglaNegocioException.class);
        comprobarSinCambios();
    }

    @Test
    void proyectoInexistenteDevuelve404SinConsultarUsuarios() {
        assertThatThrownBy(() ->
            service.actualizarAsignaciones(1L, new AsignacionesDTO(null, List.of(), List.of()))
        ).isInstanceOfSatisfying(ResponseStatusException.class, error -> assertThat(error.getStatusCode().value()).isEqualTo(404));
        verify(proyectos, never()).save(any());
        verifyNoInteractions(usuarios, avance);
    }

    @Test
    void noAdminNoConsultaNiModificaElProyecto() {
        doThrow(new AccessDeniedException("Solo admin")).when(permisos).admin();
        assertThatThrownBy(() -> service.actualizarAsignaciones(1L, new AsignacionesDTO(null, List.of(), List.of()))).isInstanceOf(
            AccessDeniedException.class
        );
        verifyNoInteractions(proyectos, usuarios, avance);
    }

    private void comprobarSinCambios() {
        assertThat(proyecto.getTutor()).isSameAs(tutorAnterior);
        assertThat(proyecto.getJurados()).containsExactly(juradoAnterior);
        assertThat(proyecto.getEstudiantes()).containsExactly(estudianteAnterior);
        assertThat(proyecto.getFechaActualizacion()).isEqualTo(Instant.EPOCH);
        verify(proyectos, never()).save(any());
        verifyNoInteractions(avance);
    }

    private User usuario(Long id, String rol) {
        User usuario = new User();
        usuario.setId(id);
        usuario.setLogin("usuario-" + id);
        usuario.setActivated(true);
        usuario.setAuthorities(Set.of(new Authority().name(rol)));
        return usuario;
    }
}
