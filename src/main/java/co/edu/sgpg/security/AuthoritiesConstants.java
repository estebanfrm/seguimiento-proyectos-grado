package co.edu.sgpg.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    /** Estudiante: registra propuestas y entregas de su propio proyecto. */
    public static final String ESTUDIANTE = "ROLE_ESTUDIANTE";

    /** Tutor: revisa entregas, añade observaciones y solicita el avance de etapa. */
    public static final String TUTOR = "ROLE_TUTOR";

    /** Jurado: evalúa las etapas evaluables de los proyectos asignados. */
    public static final String JURADO = "ROLE_JURADO";

    private AuthoritiesConstants() {}
}
