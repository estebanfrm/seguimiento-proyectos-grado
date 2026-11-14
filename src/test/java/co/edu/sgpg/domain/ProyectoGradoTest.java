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
    void entregaTest() {
        ProyectoGrado proyectoGrado = getProyectoGradoRandomSampleGenerator();
        Entrega entregaBack = getEntregaRandomSampleGenerator();

        proyectoGrado.addEntrega(entregaBack);
        assertThat(proyectoGrado.getEntregas()).containsOnly(entregaBack);
        assertThat(entregaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.removeEntrega(entregaBack);
        assertThat(proyectoGrado.getEntregas()).doesNotContain(entregaBack);
        assertThat(entregaBack.getProyecto()).isNull();

        proyectoGrado.entregas(new HashSet<>(Set.of(entregaBack)));
        assertThat(proyectoGrado.getEntregas()).containsOnly(entregaBack);
        assertThat(entregaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.setEntregas(new HashSet<>());
        assertThat(proyectoGrado.getEntregas()).doesNotContain(entregaBack);
        assertThat(entregaBack.getProyecto()).isNull();
    }

    @Test
    void fechaLimiteTest() {
        ProyectoGrado proyectoGrado = getProyectoGradoRandomSampleGenerator();
        FechaLimiteEtapa fechaLimiteEtapaBack = getFechaLimiteEtapaRandomSampleGenerator();

        proyectoGrado.addFechaLimite(fechaLimiteEtapaBack);
        assertThat(proyectoGrado.getFechaLimites()).containsOnly(fechaLimiteEtapaBack);
        assertThat(fechaLimiteEtapaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.removeFechaLimite(fechaLimiteEtapaBack);
        assertThat(proyectoGrado.getFechaLimites()).doesNotContain(fechaLimiteEtapaBack);
        assertThat(fechaLimiteEtapaBack.getProyecto()).isNull();

        proyectoGrado.fechaLimites(new HashSet<>(Set.of(fechaLimiteEtapaBack)));
        assertThat(proyectoGrado.getFechaLimites()).containsOnly(fechaLimiteEtapaBack);
        assertThat(fechaLimiteEtapaBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.setFechaLimites(new HashSet<>());
        assertThat(proyectoGrado.getFechaLimites()).doesNotContain(fechaLimiteEtapaBack);
        assertThat(fechaLimiteEtapaBack.getProyecto()).isNull();
    }

    @Test
    void evaluacionTest() {
        ProyectoGrado proyectoGrado = getProyectoGradoRandomSampleGenerator();
        Evaluacion evaluacionBack = getEvaluacionRandomSampleGenerator();

        proyectoGrado.addEvaluacion(evaluacionBack);
        assertThat(proyectoGrado.getEvaluacions()).containsOnly(evaluacionBack);
        assertThat(evaluacionBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.removeEvaluacion(evaluacionBack);
        assertThat(proyectoGrado.getEvaluacions()).doesNotContain(evaluacionBack);
        assertThat(evaluacionBack.getProyecto()).isNull();

        proyectoGrado.evaluacions(new HashSet<>(Set.of(evaluacionBack)));
        assertThat(proyectoGrado.getEvaluacions()).containsOnly(evaluacionBack);
        assertThat(evaluacionBack.getProyecto()).isEqualTo(proyectoGrado);

        proyectoGrado.setEvaluacions(new HashSet<>());
        assertThat(proyectoGrado.getEvaluacions()).doesNotContain(evaluacionBack);
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
