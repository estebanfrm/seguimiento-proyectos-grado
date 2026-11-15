package co.edu.sgpg.service;

import co.edu.sgpg.domain.ProyectoGrado;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/** Subconsulta que evita duplicar filas o alterar los filtros/paginación del CRUD. */
@Service
public class VisibilidadProyectoService {

    private final ProyectoAccessService acceso;

    public VisibilidadProyectoService(ProyectoAccessService acceso) {
        this.acceso = acceso;
    }

    public <T> Specification<T> para(Function<Root<T>, Path<Long>> proyectoId) {
        if (acceso.esAdmin()) return Specification.unrestricted();
        String login = acceso.loginActual().orElse("");
        return (root, query, cb) -> {
            var visibles = query.subquery(Long.class);
            var proyecto = visibles.from(ProyectoGrado.class);
            visibles
                .select(proyecto.get("id"))
                .where(
                    cb.or(
                        cb.equal(proyecto.join("estudiantes", JoinType.LEFT).get("login"), login),
                        cb.equal(proyecto.join("tutor", JoinType.LEFT).get("login"), login),
                        cb.equal(proyecto.join("jurados", JoinType.LEFT).get("login"), login)
                    )
                );
            return proyectoId.apply(root).in(visibles);
        };
    }
}
