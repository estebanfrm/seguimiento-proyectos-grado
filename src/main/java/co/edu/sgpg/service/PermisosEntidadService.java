package co.edu.sgpg.service;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.security.AuthoritiesConstants;
import co.edu.sgpg.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Autorización de operaciones; las relaciones se consultan mediante el contrato compartido. */
@Service
public class PermisosEntidadService {

    private final ProyectoAccessService acceso;
    private final ProyectoGradoRepository proyectos;
    private final UserRepository usuarios;

    public PermisosEntidadService(ProyectoAccessService acceso, ProyectoGradoRepository proyectos, UserRepository usuarios) {
        this.acceso = acceso;
        this.proyectos = proyectos;
        this.usuarios = usuarios;
    }

    public void admin() {
        exigir(acceso.esAdmin());
    }

    public void creadorProyecto() {
        exigir(acceso.esAdmin() || SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ESTUDIANTE));
    }

    public void estudiante(Long proyectoId) {
        exigir(SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ESTUDIANTE) && acceso.esEstudianteDe(proyectoId));
    }

    public void tutorOAdmin(Long proyectoId) {
        exigir(acceso.esAdmin() || (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.TUTOR) && acceso.esTutorDe(proyectoId)));
    }

    public void jurado(Long proyectoId) {
        exigir(SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.JURADO) && acceso.esJuradoDe(proyectoId));
    }

    public User usuarioActual() {
        return acceso
            .loginActual()
            .flatMap(usuarios::findOneByLogin)
            .orElseThrow(() -> new AccessDeniedException("Usuario no registrado"));
    }

    public ProyectoGrado proyecto(Long id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el proyecto");
        return proyectos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proyecto inexistente"));
    }

    private void exigir(boolean permitido) {
        if (!permitido) throw new AccessDeniedException("No tiene permiso para realizar esta acción");
    }
}
