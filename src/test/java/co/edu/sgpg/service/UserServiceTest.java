package co.edu.sgpg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.Authority;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.repository.AuthorityRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.security.AuthoritiesConstants;
import co.edu.sgpg.service.dto.AdminUserDTO;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorityRepository authorityRepository;

    private UserService userService;

    @BeforeEach
    void preparar() {
        userService = new UserService(userRepository, passwordEncoder, authorityRepository);
        when(passwordEncoder.encode("password")).thenReturn("encrypted-password");
        when(authorityRepository.findById(AuthoritiesConstants.USER)).thenReturn(
            Optional.of(new Authority().name(AuthoritiesConstants.USER))
        );
        when(authorityRepository.findById(AuthoritiesConstants.ESTUDIANTE)).thenReturn(
            Optional.of(new Authority().name(AuthoritiesConstants.ESTUDIANTE))
        );
    }

    static Stream<Set<String>> rolesEnviados() {
        return Stream.of(
            null,
            Set.of(),
            Set.of(AuthoritiesConstants.USER),
            Set.of(AuthoritiesConstants.ADMIN, AuthoritiesConstants.TUTOR, AuthoritiesConstants.JURADO, "ROLE_DESCONOCIDO")
        );
    }

    @ParameterizedTest
    @MethodSource("rolesEnviados")
    void registroPublicoSoloConcedeUsuarioYEstudiante(Set<String> roles) {
        AdminUserDTO solicitud = new AdminUserDTO();
        solicitud.setLogin("EstudianteNuevo");
        solicitud.setEmail("Persona@correo.example");
        solicitud.setActivated(true);
        solicitud.setAuthorities(roles);

        User registrado = userService.registerUser(solicitud, "password");

        assertThat(registrado.getAuthorities())
            .extracting(Authority::getName)
            .containsExactlyInAnyOrder(AuthoritiesConstants.USER, AuthoritiesConstants.ESTUDIANTE);
        assertThat(registrado.getLogin()).isEqualTo("estudiantenuevo");
        assertThat(registrado.getEmail()).isEqualTo("persona@correo.example");
        assertThat(registrado.getPassword()).isEqualTo("encrypted-password");
        assertThat(registrado.isActivated()).isFalse();
        assertThat(registrado.getActivationKey()).isNotBlank();
        verify(userRepository).save(registrado);
        verify(authorityRepository).findById(AuthoritiesConstants.USER);
        verify(authorityRepository).findById(AuthoritiesConstants.ESTUDIANTE);
        verifyNoMoreInteractions(authorityRepository);
    }
}
