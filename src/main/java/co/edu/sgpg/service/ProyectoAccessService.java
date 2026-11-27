package co.edu.sgpg.service;

import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.security.AuthoritiesConstants;
import co.edu.sgpg.security.SecurityUtils;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Punto único para decidir la relación del usuario actual con un proyecto de grado.
 * Todos los permisos por proyecto deben pasar por aquí.
 */
@Service
@Transactional(readOnly = true)
public class ProyectoAccessService {

    private final ProyectoGradoRepository proyectoGradoRepository;

    public ProyectoAccessService(ProyectoGradoRepository proyectoGradoRepository) {
        this.proyectoGradoRepository = proyectoGradoRepository;
    }

    public Optional<String> loginActual() {
        return SecurityUtils.getCurrentUserLogin();
    }

    public boolean esAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    public boolean esEstudianteDe(Long proyectoId) {
        return loginActual()
            .map(login -> proyectoGradoRepository.existsByIdAndEstudiantesLogin(proyectoId, login))
            .orElse(false);
    }

    public boolean esTutorDe(Long proyectoId) {
        return loginActual()
            .map(login -> proyectoGradoRepository.existsByIdAndTutorLogin(proyectoId, login))
            .orElse(false);
    }

    public boolean esJuradoDe(Long proyectoId) {
        return loginActual()
            .map(login -> proyectoGradoRepository.existsByIdAndJuradosLogin(proyectoId, login))
            .orElse(false);
    }

    /** Admin o cualquier relación (estudiante, tutor, jurado) con el proyecto. */
    public boolean puedeVer(Long proyectoId) {
        return esAdmin() || esEstudianteDe(proyectoId) || esTutorDe(proyectoId) || esJuradoDe(proyectoId);
    }

    public void verificarPuedeVer(Long proyectoId) {
        if (!puedeVer(proyectoId)) {
            throw new AccessDeniedException("No tiene acceso al proyecto " + proyectoId);
        }
    }

    /** Admin o tutor del proyecto. */
    public void verificarTutorOAdmin(Long proyectoId) {
        if (!esAdmin() && !esTutorDe(proyectoId)) {
            throw new AccessDeniedException("Solo el tutor del proyecto o el coordinador pueden realizar esta acción");
        }
    }
}
