package co.edu.sgpg.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FechaLimiteEtapaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FechaLimiteEtapaDTO.class);
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO1 = new FechaLimiteEtapaDTO();
        fechaLimiteEtapaDTO1.setId(1L);
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO2 = new FechaLimiteEtapaDTO();
        assertThat(fechaLimiteEtapaDTO1).isNotEqualTo(fechaLimiteEtapaDTO2);
        fechaLimiteEtapaDTO2.setId(fechaLimiteEtapaDTO1.getId());
        assertThat(fechaLimiteEtapaDTO1).isEqualTo(fechaLimiteEtapaDTO2);
        fechaLimiteEtapaDTO2.setId(2L);
        assertThat(fechaLimiteEtapaDTO1).isNotEqualTo(fechaLimiteEtapaDTO2);
        fechaLimiteEtapaDTO1.setId(null);
        assertThat(fechaLimiteEtapaDTO1).isNotEqualTo(fechaLimiteEtapaDTO2);
    }
}
