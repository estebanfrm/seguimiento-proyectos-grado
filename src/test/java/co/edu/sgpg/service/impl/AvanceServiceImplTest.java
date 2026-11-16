package co.edu.sgpg.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.service.dto.workflow.RequisitoDTO;
import co.edu.sgpg.service.dto.workflow.RequisitosTransicionDTO;
import co.edu.sgpg.service.workflow.EvaluadorRequisitos;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AvanceServiceImplTest {

    @Mock
    private ProyectoGradoRepository proyectos;

    @Mock
    private EvaluadorRequisitos evaluador;

    private AvanceServiceImpl service;

    @BeforeEach
    void preparar() {
        service = new AvanceServiceImpl(proyectos, evaluador);
    }

    @ParameterizedTest
    @CsvSource({
        "PROPUESTA,0,4,0",
        "PROPUESTA,1,4,4",
        "PROPUESTA,4,4,16",
        "ANTEPROYECTO,0,5,20",
        "ANTEPROYECTO,3,5,30",
        "ANTEPROYECTO,4,5,33",
        "ANTEPROYECTO,5,5,36",
        "DESARROLLO,0,3,40",
        "DESARROLLO,1,3,48",
        "DESARROLLO,2,3,57",
        "DESARROLLO,3,3,66",
        "SUSTENTACION,0,1,75",
        "SUSTENTACION,1,1,87",
    })
    void calculaFormulaYRedondeaHaciaAbajo(Etapa etapa, int cumplidos, int total, int esperado) {
        var proyecto = new ProyectoGrado().id(1L).etapaActual(etapa).estado(EstadoProyecto.ACTIVO);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        var requisitos = IntStream.range(0, total)
            .mapToObj(i -> new RequisitoDTO("R" + i, "Requisito", i < cumplidos))
            .toList();
        when(evaluador.evaluar(proyecto)).thenReturn(
            new RequisitosTransicionDTO(1L, etapa, Etapa.values()[etapa.ordinal() + 1], true, requisitos)
        );
        assertThat(service.recalcular(1L)).isEqualTo(esperado);
        assertThat(proyecto.getPorcentajeAvance()).isEqualTo(esperado);
        verify(proyectos).save(proyecto);
    }

    @Test
    void finalizadoSiempreTieneCienPorCiento() {
        var proyecto = new ProyectoGrado().id(1L).etapaActual(Etapa.FINALIZADO).estado(EstadoProyecto.REPROBADO);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        assertThat(service.recalcular(1L)).isEqualTo(100);
        verifyNoInteractions(evaluador);
        verify(proyectos).save(proyecto);
    }

    @Test
    void sinRequisitosConservaLaBaseYNoDividePorCero() {
        var proyecto = new ProyectoGrado().id(1L).etapaActual(Etapa.DESARROLLO).estado(EstadoProyecto.CANCELADO);
        when(proyectos.findById(1L)).thenReturn(Optional.of(proyecto));
        when(evaluador.evaluar(proyecto)).thenReturn(
            new RequisitosTransicionDTO(1L, Etapa.DESARROLLO, Etapa.SUSTENTACION, false, List.of())
        );
        assertThat(service.recalcular(1L)).isEqualTo(40);
    }

    @Test
    void proyectoInexistenteDevuelve404() {
        assertThatThrownBy(() -> service.recalcular(1L)).isInstanceOfSatisfying(ResponseStatusException.class, e ->
            assertThat(e.getStatusCode().value()).isEqualTo(404)
        );
    }
}
