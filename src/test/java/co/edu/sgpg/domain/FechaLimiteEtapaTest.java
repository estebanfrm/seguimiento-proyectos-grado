package co.edu.sgpg.domain;

import static co.edu.sgpg.domain.FechaLimiteEtapaTestSamples.*;
import static co.edu.sgpg.domain.ProyectoGradoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FechaLimiteEtapaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FechaLimiteEtapa.class);
        FechaLimiteEtapa fechaLimiteEtapa1 = getFechaLimiteEtapaSample1();
        FechaLimiteEtapa fechaLimiteEtapa2 = new FechaLimiteEtapa();
        assertThat(fechaLimiteEtapa1).isNotEqualTo(fechaLimiteEtapa2);

        fechaLimiteEtapa2.setId(fechaLimiteEtapa1.getId());
        assertThat(fechaLimiteEtapa1).isEqualTo(fechaLimiteEtapa2);

        fechaLimiteEtapa2 = getFechaLimiteEtapaSample2();
        assertThat(fechaLimiteEtapa1).isNotEqualTo(fechaLimiteEtapa2);
    }

    @Test
    void proyectoTest() {
        FechaLimiteEtapa fechaLimiteEtapa = getFechaLimiteEtapaRandomSampleGenerator();
        ProyectoGrado proyectoGradoBack = getProyectoGradoRandomSampleGenerator();

        fechaLimiteEtapa.setProyecto(proyectoGradoBack);
        assertThat(fechaLimiteEtapa.getProyecto()).isEqualTo(proyectoGradoBack);

        fechaLimiteEtapa.proyecto(null);
        assertThat(fechaLimiteEtapa.getProyecto()).isNull();
    }
}
