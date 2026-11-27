package co.edu.sgpg.service.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.enumeration.Concepto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReglaSustentacionAFinalizadoTest extends ReglasTestSupport {

    @Test
    void permiteFinalizarConEvaluacionesReprobatorias() {
        var a = usuario(2);
        var proyecto = proyecto(Etapa.SUSTENTACION).addJurado(a);
        when(evaluaciones.findByProyectoIdAndEtapa(1L, Etapa.SUSTENTACION)).thenReturn(List.of(nota(a, "2", Concepto.REPROBADO)));
        var regla = new ReglaSustentacionAFinalizado(requisitos);
        assertThat(regla.origen()).isEqualTo(Etapa.SUSTENTACION);
        assertThat(regla.destino()).isEqualTo(Etapa.FINALIZADO);
        assertThat(regla.evaluar(proyecto)).extracting(RequisitoDTO::codigo).containsExactly("EVALUACIONES_COMPLETAS");
        assertThat(regla.evaluar(proyecto)).allMatch(RequisitoDTO::cumplido);
    }

    @Test
    void noFinalizaSinJurados() {
        assertThat(new ReglaSustentacionAFinalizado(requisitos).evaluar(proyecto(Etapa.SUSTENTACION))).noneMatch(RequisitoDTO::cumplido);
    }

    @ParameterizedTest
    @CsvSource({ "3.0,3.0,APROBADO,true", "2.999,3.0,APROBADO,false", "5,2.9,REPROBADO,false", "5,4,APROBADO_CON_MODIFICACIONES,true" })
    void calculaResultadoExactoSinRedondear(String primera, String segunda, Concepto concepto, boolean aprobado) {
        when(evaluaciones.findByProyectoIdAndEtapa(1L, Etapa.SUSTENTACION)).thenReturn(
            List.of(nota(usuario(2), primera, Concepto.APROBADO), nota(usuario(3), segunda, concepto))
        );
        assertThat(requisitos.resultadoAprobatorio(proyecto(Etapa.SUSTENTACION))).isEqualTo(aprobado);
    }
}
