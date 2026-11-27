package co.edu.sgpg.domain;

import static co.edu.sgpg.domain.EntregaTestSamples.*;
import static co.edu.sgpg.domain.ObservacionTestSamples.*;
import static co.edu.sgpg.domain.ProyectoGradoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EntregaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Entrega.class);
        Entrega entrega1 = getEntregaSample1();
        Entrega entrega2 = new Entrega();
        assertThat(entrega1).isNotEqualTo(entrega2);

        entrega2.setId(entrega1.getId());
        assertThat(entrega1).isEqualTo(entrega2);

        entrega2 = getEntregaSample2();
        assertThat(entrega1).isNotEqualTo(entrega2);
    }

    @Test
    void observacionTest() {
        Entrega entrega = getEntregaRandomSampleGenerator();
        Observacion observacionBack = getObservacionRandomSampleGenerator();

        entrega.addObservacion(observacionBack);
        assertThat(entrega.getObservacions()).containsOnly(observacionBack);
        assertThat(observacionBack.getEntrega()).isEqualTo(entrega);

        entrega.removeObservacion(observacionBack);
        assertThat(entrega.getObservacions()).doesNotContain(observacionBack);
        assertThat(observacionBack.getEntrega()).isNull();

        entrega.observacions(new HashSet<>(Set.of(observacionBack)));
        assertThat(entrega.getObservacions()).containsOnly(observacionBack);
        assertThat(observacionBack.getEntrega()).isEqualTo(entrega);

        entrega.setObservacions(new HashSet<>());
        assertThat(entrega.getObservacions()).doesNotContain(observacionBack);
        assertThat(observacionBack.getEntrega()).isNull();
    }

    @Test
    void proyectoTest() {
        Entrega entrega = getEntregaRandomSampleGenerator();
        ProyectoGrado proyectoGradoBack = getProyectoGradoRandomSampleGenerator();

        entrega.setProyecto(proyectoGradoBack);
        assertThat(entrega.getProyecto()).isEqualTo(proyectoGradoBack);

        entrega.proyecto(null);
        assertThat(entrega.getProyecto()).isNull();
    }
}
