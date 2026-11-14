package co.edu.sgpg.domain;

import static co.edu.sgpg.domain.EvaluacionTestSamples.*;
import static co.edu.sgpg.domain.ProyectoGradoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EvaluacionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Evaluacion.class);
        Evaluacion evaluacion1 = getEvaluacionSample1();
        Evaluacion evaluacion2 = new Evaluacion();
        assertThat(evaluacion1).isNotEqualTo(evaluacion2);

        evaluacion2.setId(evaluacion1.getId());
        assertThat(evaluacion1).isEqualTo(evaluacion2);

        evaluacion2 = getEvaluacionSample2();
        assertThat(evaluacion1).isNotEqualTo(evaluacion2);
    }

    @Test
    void proyectoTest() {
        Evaluacion evaluacion = getEvaluacionRandomSampleGenerator();
        ProyectoGrado proyectoGradoBack = getProyectoGradoRandomSampleGenerator();

        evaluacion.setProyecto(proyectoGradoBack);
        assertThat(evaluacion.getProyecto()).isEqualTo(proyectoGradoBack);

        evaluacion.proyecto(null);
        assertThat(evaluacion.getProyecto()).isNull();
    }
}
