package co.edu.sgpg.web.rest;

import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.ReglaNegocioException;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.dto.workflow.*;
import co.edu.sgpg.service.workflow.MotorEtapasService;
import co.edu.sgpg.service.workflow.SeguimientoService;
import co.edu.sgpg.web.rest.errors.ExceptionTranslator;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

/** Verifica el contrato JSON y la traducción de errores sin contexto Spring ni base de datos. */
@ExtendWith(MockitoExtension.class)
class ProyectoWorkflowResourceTest {

    @Mock
    private MotorEtapasService motor;

    @Mock
    private SeguimientoService seguimiento;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new ProyectoWorkflowResource(motor, seguimiento))
            .setControllerAdvice(new ExceptionTranslator(new MockEnvironment()))
            .build();
    }

    @Test
    void serializaWorkflowYSeguimientoConLosNombresDelContrato() throws Exception {
        var requisitos = new RequisitosTransicionDTO(
            1L,
            Etapa.PROPUESTA,
            Etapa.ANTEPROYECTO,
            false,
            List.of(new RequisitoDTO("ENTREGA_APROBADA", "Existe una entrega aprobada", false))
        );
        when(motor.requisitos(1L)).thenReturn(requisitos);
        mvc.perform(get("/api/proyecto-grados/1/workflow"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.proyectoId").value(1))
            .andExpect(jsonPath("$.etapaActual").value("PROPUESTA"))
            .andExpect(jsonPath("$.etapaSiguiente").value("ANTEPROYECTO"))
            .andExpect(jsonPath("$.puedeAvanzar").value(false))
            .andExpect(jsonPath("$.requisitos[0].codigo").value("ENTREGA_APROBADA"))
            .andExpect(jsonPath("$.requisitos[0].descripcion").value("Existe una entrega aprobada"))
            .andExpect(jsonPath("$.requisitos[0].cumplido").value(false));

        var proyecto = new ProyectoGradoDTO();
        proyecto.setId(1L);
        var fecha = Instant.parse("2026-11-13T15:00:00Z");
        when(seguimiento.obtener(1L)).thenReturn(
            new SeguimientoProyectoDTO(
                proyecto,
                15,
                List.of(
                    new EtapaSeguimientoDTO(
                        Etapa.PROPUESTA,
                        "ACTUAL",
                        fecha,
                        true,
                        -1L,
                        List.of(new EntregaResumenDTO(2L, "Propuesta", 1, EstadoEntrega.ENVIADA, fecha, 2L)),
                        List.of()
                    )
                ),
                List.of(),
                requisitos
            )
        );
        mvc.perform(get("/api/proyecto-grados/1/seguimiento"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.proyecto.id").value(1))
            .andExpect(jsonPath("$.porcentajeAvance").value(15))
            .andExpect(jsonPath("$.etapas[0].etapa").value("PROPUESTA"))
            .andExpect(jsonPath("$.etapas[0].estado").value("ACTUAL"))
            .andExpect(jsonPath("$.etapas[0].fechaLimite").value(fecha.toString()))
            .andExpect(jsonPath("$.etapas[0].vencida").value(true))
            .andExpect(jsonPath("$.etapas[0].diasRestantes").value(-1))
            .andExpect(jsonPath("$.etapas[0].entregas[0].id").value(2))
            .andExpect(jsonPath("$.etapas[0].entregas[0].observacionesPendientes").value(2))
            .andExpect(jsonPath("$.etapas[0].evaluaciones").isEmpty())
            .andExpect(jsonPath("$.historial").isEmpty())
            .andExpect(jsonPath("$.requisitos.proyectoId").value(1));
    }

    @Test
    void avanzarAceptaComentarioOpcionalYCancelarLoDelega() throws Exception {
        mvc.perform(post("/api/proyecto-grados/1/workflow/avanzar")).andExpect(status().isOk());
        verify(motor).avanzar(1L, null);
        mvc.perform(
            post("/api/proyecto-grados/1/workflow/avanzar").contentType(MediaType.APPLICATION_JSON).content("{\"comentario\":\"Revisado\"}")
        ).andExpect(status().isOk());
        verify(motor).avanzar(1L, "Revisado");
        mvc.perform(
            post("/api/proyecto-grados/1/workflow/cancelar").contentType(MediaType.APPLICATION_JSON).content("{\"comentario\":\"Retiro\"}")
        ).andExpect(status().isOk());
        verify(motor).cancelar(1L, "Retiro");
    }

    @Test
    void pendientesSon409ConCodigosEstables() throws Exception {
        when(motor.avanzar(1L, null)).thenThrow(
            new ReglaNegocioException("REQUISITOS_PENDIENTES", "Faltan requisitos", List.of("TUTOR_ASIGNADO"))
        );
        mvc.perform(post("/api/proyecto-grados/1/workflow/avanzar"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("REQUISITOS_PENDIENTES"))
            .andExpect(jsonPath("$.message").value("error.reglaNegocio"))
            .andExpect(jsonPath("$.pendientes", contains("TUTOR_ASIGNADO")));
    }

    @Test
    void traduceFaltaDePermisoYProyectoInexistente() throws Exception {
        when(seguimiento.obtener(1L)).thenThrow(new AccessDeniedException("Sin acceso"));
        mvc.perform(get("/api/proyecto-grados/1/seguimiento")).andExpect(status().isForbidden());
        when(motor.requisitos(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "El proyecto no existe"));
        mvc.perform(get("/api/proyecto-grados/99/workflow")).andExpect(status().isNotFound());
    }

    @Test
    void validaLongitudDelComentarioAntesDeInvocarElMotor() throws Exception {
        mvc.perform(
            post("/api/proyecto-grados/1/workflow/cancelar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comentario\":\"" + "x".repeat(501) + "\"}")
        ).andExpect(status().isBadRequest());
        verifyNoInteractions(motor);
    }
}
