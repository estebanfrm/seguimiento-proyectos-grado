package co.edu.sgpg.domain;

import static co.edu.sgpg.domain.EntregaTestSamples.*;
import static co.edu.sgpg.domain.ObservacionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ObservacionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Observacion.class);
        Observacion observacion1 = getObservacionSample1();
        Observacion observacion2 = new Observacion();
        assertThat(observacion1).isNotEqualTo(observacion2);

        observacion2.setId(observacion1.getId());
        assertThat(observacion1).isEqualTo(observacion2);

        observacion2 = getObservacionSample2();
        assertThat(observacion1).isNotEqualTo(observacion2);
    }

    @Test
    void entregaTest() {
        Observacion observacion = getObservacionRandomSampleGenerator();
        Entrega entregaBack = getEntregaRandomSampleGenerator();

        observacion.setEntrega(entregaBack);
        assertThat(observacion.getEntrega()).isEqualTo(entregaBack);

        observacion.entrega(null);
        assertThat(observacion.getEntrega()).isNull();
    }
}
