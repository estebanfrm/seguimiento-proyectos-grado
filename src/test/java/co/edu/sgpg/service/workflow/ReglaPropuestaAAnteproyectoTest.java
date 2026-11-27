package co.edu.sgpg.service.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.domain.enumeration.TipoObservacion;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import org.junit.jupiter.api.Test;

class ReglaPropuestaAAnteproyectoTest extends ReglasTestSupport {

    @Test
    void evaluaTodosLosRequisitosYLosCodigosEnOrden() {
        var proyecto = proyecto(Etapa.PROPUESTA).tutor(usuario(2)).addEstudiante(usuario(3));
        when(entregas.existsByProyectoIdAndEtapaAndEstado(1L, Etapa.PROPUESTA, EstadoEntrega.APROBADA)).thenReturn(true);
        var regla = new ReglaPropuestaAAnteproyecto(requisitos);
        assertThat(regla.origen()).isEqualTo(Etapa.PROPUESTA);
        assertThat(regla.destino()).isEqualTo(Etapa.ANTEPROYECTO);
        var resultado = regla.evaluar(proyecto);
        assertThat(resultado)
            .extracting(RequisitoDTO::codigo)
            .containsExactly("TUTOR_ASIGNADO", "ESTUDIANTES_ASIGNADOS", "ENTREGA_APROBADA", "SIN_CORRECCIONES_PENDIENTES");
        assertThat(resultado).allMatch(RequisitoDTO::cumplido);
    }

    @Test
    void detectaTutorEstudianteEntregaYCorreccionesFaltantes() {
        when(
            observaciones.countByEntregaProyectoIdAndEntregaEtapaAndTipoAndResueltaFalse(
                1L,
                Etapa.PROPUESTA,
                TipoObservacion.CORRECCION_REQUERIDA
            )
        ).thenReturn(1L);
        assertThat(new ReglaPropuestaAAnteproyecto(requisitos).evaluar(proyecto(Etapa.PROPUESTA))).noneMatch(RequisitoDTO::cumplido);
    }
}
