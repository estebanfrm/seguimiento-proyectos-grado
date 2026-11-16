package co.edu.sgpg.service.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.FechaLimiteEtapa;
import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReglaDesarrolloASustentacionTest extends ReglasTestSupport {

    @Test
    void exigeDocumentoFinalSinCorreccionesYFechaDeSustentacion() {
        when(entregas.existsByProyectoIdAndEtapaAndEstado(1L, Etapa.DESARROLLO, EstadoEntrega.APROBADA)).thenReturn(true);
        when(fechas.findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(1L, Etapa.SUSTENTACION)).thenReturn(
            Optional.of(new FechaLimiteEtapa())
        );
        var regla = new ReglaDesarrolloASustentacion(requisitos);
        assertThat(regla.origen()).isEqualTo(Etapa.DESARROLLO);
        assertThat(regla.destino()).isEqualTo(Etapa.SUSTENTACION);
        var resultado = regla.evaluar(proyecto(Etapa.DESARROLLO));
        assertThat(resultado)
            .extracting(RequisitoDTO::codigo)
            .containsExactly("ENTREGA_APROBADA", "SIN_CORRECCIONES_PENDIENTES", "SUSTENTACION_PROGRAMADA");
        assertThat(resultado).allMatch(RequisitoDTO::cumplido);
    }

    @Test
    void informaQueFaltaProgramarSustentacion() {
        var resultado = new ReglaDesarrolloASustentacion(requisitos).evaluar(proyecto(Etapa.DESARROLLO));
        assertThat(resultado.get(2).cumplido()).isFalse();
        verify(fechas).findFirstByProyectoIdAndEtapaOrderByFechaLimiteDesc(1L, Etapa.SUSTENTACION);
    }
}
