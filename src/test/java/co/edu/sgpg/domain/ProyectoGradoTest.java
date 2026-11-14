package co.edu.sgpg.domain;

import static co.edu.sgpg.domain.EntregaTestSamples.*;
import static co.edu.sgpg.domain.EvaluacionTestSamples.*;
import static co.edu.sgpg.domain.FechaLimiteEtapaTestSamples.*;
import static co.edu.sgpg.domain.HistorialEtapaTestSamples.*;
import static co.edu.sgpg.domain.ProyectoGradoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProyectoGradoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProyectoGrado.class);
        ProyectoGrado proyectoGrado1 = getProyectoGradoSample1();
        ProyectoGrado proyectoGrado2 = new ProyectoGrado();
        assertThat(proyectoGrado1).isNotEqualTo(proyectoGrado2);

        proyectoGrado2.setId(proyectoGrado1.getId());
        assertThat(proyectoGrado1).isEqualTo(proyectoGrado2);

        proyectoGrado2 = getProyectoGradoSample2();
        assertThat(proyectoGrado1).isNotEqualTo(proyectoGrado2);
    }

    @Test
    void entregasTest() {
        ProyectoGrado proyectoGrado = getProyectoGradoRandomSampleGenerator();
        Entrega entregaBack = getEntregaRandomSampleGenerator();

        proyectoGrado.addEntregas(entregaBack);
        assertThat(proyectoGrado.getEntregases()).containsOnly(entregaBack);
        assertThat(entregaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.removeEntregas(entregaBack);
        assertThat(proyectoGrado.getEntregases()).doesNotContain(entregaBack);
        assertThat(entregaBack.getProyecto()).isNull();

        proyectoGrado.entregases(new HashSet<>(Set.of(entregaBack)));
        assertThat(proyectoGrado.getEntregases()).containsOnly(entregaBack);
        assertThat(entregaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.setEntregases(new HashSet<>());
        assertThat(proyectoGrado.getEntregases()).doesNotContain(entregaBack);
        assertThat(entregaBack.getProyecto()).isNull();
    }

    @Test
    void fechasLimiteTest() {
        ProyectoGrado proyectoGrado = getProyectoGradoRandomSampleGenerator();
        FechaLimiteEtapa fechaLimiteEtapaBack = getFechaLimiteEtapaRandomSampleGenerator();

        proyectoGrado.addFechasLimite(fechaLimiteEtapaBack);
        assertThat(proyectoGrado.getFechasLimites()).containsOnly(fechaLimiteEtapaBack);
        assertThat(fechaLimiteEtapaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.removeFechasLimite(fechaLimiteEtapaBack);
        assertThat(proyectoGrado.getFechasLimites()).doesNotContain(fechaLimiteEtapaBack);
        assertThat(fechaLimiteEtapaBack.getProyecto()).isNull();

        proyectoGrado.fechasLimites(new HashSet<>(Set.of(fechaLimiteEtapaBack)));
        assertThat(proyectoGrado.getFechasLimites()).containsOnly(fechaLimiteEtapaBack);
        assertThat(fechaLimiteEtapaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.setFechasLimites(new HashSet<>());
        assertThat(proyectoGrado.getFechasLimites()).doesNotContain(fechaLimiteEtapaBack);
        assertThat(fechaLimiteEtapaBack.getProyecto()).isNull();
    }

    @Test
    void evaluacionesTest() {
        ProyectoGrado proyectoGrado = getProyectoGradoRandomSampleGenerator();
        Evaluacion evaluacionBack = getEvaluacionRandomSampleGenerator();

        proyectoGrado.addEvaluaciones(evaluacionBack);
        assertThat(proyectoGrado.getEvaluacioneses()).containsOnly(evaluacionBack);
        assertThat(evaluacionBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.removeEvaluaciones(evaluacionBack);
        assertThat(proyectoGrado.getEvaluacioneses()).doesNotContain(evaluacionBack);
        assertThat(evaluacionBack.getProyecto()).isNull();

        proyectoGrado.evaluacioneses(new HashSet<>(Set.of(evaluacionBack)));
        assertThat(proyectoGrado.getEvaluacioneses()).containsOnly(evaluacionBack);
        assertThat(evaluacionBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.setEvaluacioneses(new HashSet<>());
        assertThat(proyectoGrado.getEvaluacioneses()).doesNotContain(evaluacionBack);
        assertThat(evaluacionBack.getProyecto()).isNull();
    }

    @Test
    void historialTest() {
        ProyectoGrado proyectoGrado = getProyectoGradoRandomSampleGenerator();
        HistorialEtapa historialEtapaBack = getHistorialEtapaRandomSampleGenerator();

        proyectoGrado.addHistorial(historialEtapaBack);
        assertThat(proyectoGrado.getHistorials()).containsOnly(historialEtapaBack);
        assertThat(historialEtapaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.removeHistorial(historialEtapaBack);
        assertThat(proyectoGrado.getHistorials()).doesNotContain(historialEtapaBack);
        assertThat(historialEtapaBack.getProyecto()).isNull();

        proyectoGrado.historials(new HashSet<>(Set.of(historialEtapaBack)));
        assertThat(proyectoGrado.getHistorials()).containsOnly(historialEtapaBack);
        assertThat(historialEtapaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.setHistorials(new HashSet<>());
        assertThat(proyectoGrado.getHistorials()).doesNotContain(historialEtapaBack);
        assertThat(historialEtapaBack.getProyecto()).isNull();
    }
}
