package co.edu.sgpg.domain;

import static co.edu.sgpg.domain.HistorialEtapaTestSamples.*;
import static co.edu.sgpg.domain.ProyectoGradoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HistorialEtapaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(HistorialEtapa.class);
        HistorialEtapa historialEtapa1 = getHistorialEtapaSample1();
        HistorialEtapa historialEtapa2 = new HistorialEtapa();
        assertThat(historialEtapa1).isNotEqualTo(historialEtapa2);

        historialEtapa2.setId(historialEtapa1.getId());
        assertThat(historialEtapa1).isEqualTo(historialEtapa2);

        historialEtapa2 = getHistorialEtapaSample2();
        assertThat(historialEtapa1).isNotEqualTo(historialEtapa2);
    }

    @Test
    void proyectoTest() {
        HistorialEtapa historialEtapa = getHistorialEtapaRandomSampleGenerator();
        ProyectoGrado proyectoGradoBack = getProyectoGradoRandomSampleGenerator();

        historialEtapa.setProyecto(proyectoGradoBack);
        assertThat(historialEtapa.getProyecto()).isEqualTo(proyectoGradoBack);

        historialEtapa.proyecto(null);
        assertThat(historialEtapa.getProyecto()).isNull();
    }
}
