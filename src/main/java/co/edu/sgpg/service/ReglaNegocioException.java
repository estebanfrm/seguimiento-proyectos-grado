package co.edu.sgpg.service;

import co.edu.sgpg.web.rest.errors.ErrorConstants;
import java.io.Serial;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;

/**
 * Violación de una regla de negocio del flujo académico. Se traduce a HTTP 409 (Conflict) con un
 * ProblemDetail que incluye {@code codigo} (estable, lo consume el frontend) y, opcionalmente,
 * {@code pendientes} (códigos de requisitos no cumplidos).
 */
@SuppressWarnings("java:S110")
public class ReglaNegocioException extends ErrorResponseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final URI TYPE = URI.create(ErrorConstants.PROBLEM_BASE_URL + "/regla-negocio");

    private final String codigo;

    private final List<String> pendientes;

    public ReglaNegocioException(String codigo, String mensaje) {
        this(codigo, mensaje, List.of());
    }

    public ReglaNegocioException(String codigo, String mensaje, List<String> pendientes) {
        super(
            HttpStatus.CONFLICT,
            ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.CONFLICT.value())
                .withType(TYPE)
                .withTitle(mensaje)
                .withDetail(mensaje)
                .withProperty("message", "error.reglaNegocio")
                .withProperty("codigo", codigo)
                .withProperty("pendientes", List.copyOf(pendientes))
                .build(),
            null
        );
        this.codigo = codigo;
        this.pendientes = List.copyOf(pendientes);
    }

    public String getCodigo() {
        return codigo;
    }

    public List<String> getPendientes() {
        return pendientes;
    }

    @Override
    public String getMessage() {
        return codigo + ": " + getBody().getDetail();
    }

    public ProblemDetailWithCause getProblemDetailWithCause() {
        return (ProblemDetailWithCause) this.getBody();
    }
}
