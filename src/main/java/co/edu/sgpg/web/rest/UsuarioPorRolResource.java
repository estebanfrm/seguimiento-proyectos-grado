package co.edu.sgpg.web.rest;

import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.security.AuthoritiesConstants;
import co.edu.sgpg.service.dto.UsuarioResumenDTO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioPorRolResource {

    private final UserRepository usuarios;

    public UsuarioPorRolResource(UserRepository usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/por-rol/{authority}")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public List<UsuarioResumenDTO> porRol(@PathVariable String authority) {
        return usuarios
            .findActivosPorAuthority(authority)
            .stream()
            .map(u -> new UsuarioResumenDTO(u.getId(), u.getLogin(), u.getFirstName(), u.getLastName()))
            .toList();
    }
}
