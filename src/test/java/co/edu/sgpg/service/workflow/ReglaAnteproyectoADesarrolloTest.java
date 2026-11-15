package co.edu.sgpg.service.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.enumeration.Concepto;
import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReglaAnteproyectoADesarrolloTest extends ReglasTestSupport {

    @Test
    void requiereEntregaCorreccionesJuradosYEvaluacionesAprobatorias() {
        var a = usuario(2);
        var b = usuario(3);
        var proyecto = proyecto(Etapa.ANTEPROYECTO).addJurado(a).addJurado(b);
        when(entregas.existsByProyectoIdAndEtapaAndEstado(1L, Etapa.ANTEPROYECTO, EstadoEntrega.APROBADA)).thenReturn(true);
        when(evaluaciones.findByProyectoIdAndEtapa(1L, Etapa.ANTEPROYECTO)).thenReturn(
            List.of(nota(a, "3.0", Concepto.APROBADO), nota(b, "4.5", Concepto.APROBADO_CON_MODIFICACIONES))
        );
        var regla = new ReglaAnteproyectoADesarrollo(requisitos);
        assertThat(regla.origen()).isEqualTo(Etapa.ANTEPROYECTO);
        assertThat(regla.destino()).isEqualTo(Etapa.DESARROLLO);
        var resultado = regla.evaluar(proyecto);
        assertThat(resultado)
            .extracting(RequisitoDTO::codigo)
            .containsExactly(
                "ENTREGA_APROBADA",
                "SIN_CORRECCIONES_PENDIENTES",
                "JURADOS_ASIGNADOS",
                "EVALUACIONES_COMPLETAS",
                "EVALUACION_APROBATORIA"
            );
        assertThat(resultado).allMatch(RequisitoDTO::cumplido);
    }

    @Test
    void noCompletaEvaluacionesConDuplicadosONoRelacionados() {
        var a = usuario(2);
        var b = usuario(3);
        var proyecto = proyecto(Etapa.ANTEPROYECTO).addJurado(a).addJurado(b);
        when(evaluaciones.findByProyectoIdAndEtapa(1L, Etapa.ANTEPROYECTO)).thenReturn(
            List.of(nota(a, "5", Concepto.APROBADO), nota(a, "5", Concepto.APROBADO), nota(usuario(4), "5", Concepto.APROBADO))
        );
        assertThat(requisitos.evaluacionesCompletas(proyecto).cumplido()).isFalse();
    }

    @Test
    void sinJuradosONotasNoHayAprobacion() {
        var resultado = new ReglaAnteproyectoADesarrollo(requisitos).evaluar(proyecto(Etapa.ANTEPROYECTO));
        assertThat(resultado)
            .filteredOn(r -> !r.cumplido())
            .extracting(RequisitoDTO::codigo)
            .containsExactly("ENTREGA_APROBADA", "JURADOS_ASIGNADOS", "EVALUACIONES_COMPLETAS", "EVALUACION_APROBATORIA");
    }
}
