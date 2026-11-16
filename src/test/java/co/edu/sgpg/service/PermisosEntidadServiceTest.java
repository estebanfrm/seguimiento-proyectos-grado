package co.edu.sgpg.service;

import static co.edu.sgpg.security.AuthoritiesConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.User;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PermisosEntidadServiceTest {

    @Mock
    private ProyectoAccessService acceso;

    @Mock
    private ProyectoGradoRepository proyectos;

    @Mock
    private UserRepository usuarios;

    private PermisosEntidadService permisos;

    @BeforeEach
    void preparar() {
        permisos = new PermisosEntidadService(acceso, proyectos, usuarios);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(String authority) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("persona", "", List.of(new SimpleGrantedAuthority(authority)))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { ESTUDIANTE, TUTOR, JURADO, USER })
    void noAdminNoPuedeAdministrar(String authority) {
        autenticar(authority);
        assertThatThrownBy(permisos::admin).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminPuedeAdministrarYRevisar() {
        when(acceso.esAdmin()).thenReturn(true);
        permisos.admin();
        permisos.creadorProyecto();
        permisos.tutorOAdmin(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = { TUTOR, JURADO, USER })
    void soloEstudianteOAdminCreaPropuesta(String authority) {
        autenticar(authority);
        assertThatThrownBy(permisos::creadorProyecto).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void estudianteRequiereRelacionConElProyecto() {
        autenticar(ESTUDIANTE);
        permisos.creadorProyecto();
        assertThatThrownBy(() -> permisos.estudiante(1L)).isInstanceOf(AccessDeniedException.class);
        when(acceso.esEstudianteDe(1L)).thenReturn(true);
        permisos.estudiante(1L);
    }

    @Test
    void tutorRequiereRelacionConElProyecto() {
        autenticar(TUTOR);
        assertThatThrownBy(() -> permisos.tutorOAdmin(1L)).isInstanceOf(AccessDeniedException.class);
        when(acceso.esTutorDe(1L)).thenReturn(true);
        permisos.tutorOAdmin(1L);
    }

    @Test
    void juradoRequiereRelacionConElProyecto() {
        autenticar(JURADO);
        assertThatThrownBy(() -> permisos.jurado(1L)).isInstanceOf(AccessDeniedException.class);
        when(acceso.esJuradoDe(1L)).thenReturn(true);
        permisos.jurado(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = { ADMIN, TUTOR, JURADO, USER })
    void relacionSinRolEstudianteNoAutorizaEntrega(String authority) {
        autenticar(authority);
        assertThatThrownBy(() -> permisos.estudiante(1L)).isInstanceOf(AccessDeniedException.class);
        verify(acceso, never()).esEstudianteDe(1L);
    }

    @Test
    void usuarioActualSeObtienePorLogin() {
        User persona = new User();
        persona.setLogin("persona");
        when(acceso.loginActual()).thenReturn(Optional.of("persona"));
        when(usuarios.findOneByLogin("persona")).thenReturn(Optional.of(persona));
        assertThat(permisos.usuarioActual()).isSameAs(persona);
    }

    @Test
    void usuarioNoRegistradoEsProhibido() {
        when(acceso.loginActual()).thenReturn(Optional.of("sin-registro"));
        assertThatThrownBy(permisos::usuarioActual).isInstanceOf(AccessDeniedException.class);
    }
}
