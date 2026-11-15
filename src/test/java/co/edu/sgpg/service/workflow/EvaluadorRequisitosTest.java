package co.edu.sgpg.service.workflow;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EvaluadorRequisitosTest {

    @ParameterizedTest
    @EnumSource(value = Etapa.class, names = "FINALIZADO", mode = EnumSource.Mode.EXCLUDE)
    void seleccionaReglaDeCadaEtapa(Etapa etapa) {
        ReglaTransicion regla = mock(ReglaTransicion.class);
        when(regla.origen()).thenReturn(etapa);
        when(regla.destino()).thenReturn(Etapa.values()[etapa.ordinal() + 1]);
        var proyecto = new ProyectoGrado().id(1L).etapaActual(etapa).estado(EstadoProyecto.ACTIVO);
        when(regla.evaluar(proyecto)).thenReturn(List.of(new RequisitoDTO("REQUISITO", "Requisito", true)));
        var resultado = new EvaluadorRequisitos(List.of(regla)).evaluar(proyecto);
        assertThat(resultado.proyectoId()).isEqualTo(1L);
        assertThat(resultado.etapaActual()).isEqualTo(etapa);
        assertThat(resultado.etapaSiguiente()).isEqualTo(Etapa.values()[etapa.ordinal() + 1]);
        assertThat(resultado.puedeAvanzar()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(EstadoProyecto.class)
    void respetaEstadoDelProyecto(EstadoProyecto estado) {
        var regla = mock(ReglaTransicion.class);
        when(regla.origen()).thenReturn(Etapa.PROPUESTA);
        var proyecto = new ProyectoGrado().etapaActual(Etapa.PROPUESTA).estado(estado);
        when(regla.evaluar(proyecto)).thenReturn(List.of(new RequisitoDTO("TUTOR_ASIGNADO", "Tutor", true)));
        assertThat(new EvaluadorRequisitos(List.of(regla)).evaluar(proyecto).puedeAvanzar()).isEqualTo(estado == EstadoProyecto.ACTIVO);
    }

    @Test
    void requisitoPendienteImpideAvanzar() {
        var regla = mock(ReglaTransicion.class);
        when(regla.origen()).thenReturn(Etapa.PROPUESTA);
        var proyecto = new ProyectoGrado().etapaActual(Etapa.PROPUESTA).estado(EstadoProyecto.ACTIVO);
        when(regla.evaluar(proyecto)).thenReturn(List.of(new RequisitoDTO("ENTREGA_APROBADA", "Entrega", false)));
        assertThat(new EvaluadorRequisitos(List.of(regla)).evaluar(proyecto).puedeAvanzar()).isFalse();
    }

    @Test
    void finalizadoEsTerminalInclusoSiEstaActivo() {
        var resultado = new EvaluadorRequisitos(List.of()).evaluar(
            new ProyectoGrado().id(1L).etapaActual(Etapa.FINALIZADO).estado(EstadoProyecto.ACTIVO)
        );
        assertThat(resultado.etapaSiguiente()).isNull();
        assertThat(resultado.puedeAvanzar()).isFalse();
        assertThat(resultado.requisitos()).isEmpty();
    }

    @Test
    void rechazaReglasDuplicadasYEtapasSinRegla() {
        var regla = mock(ReglaTransicion.class);
        when(regla.origen()).thenReturn(Etapa.PROPUESTA);
        assertThatIllegalArgumentException().isThrownBy(() -> new EvaluadorRequisitos(List.of(regla, regla)));
        assertThatIllegalStateException().isThrownBy(() ->
            new EvaluadorRequisitos(List.of()).evaluar(new ProyectoGrado().etapaActual(Etapa.DESARROLLO))
        );
    }
}
