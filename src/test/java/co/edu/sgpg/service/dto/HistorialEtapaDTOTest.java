package co.edu.sgpg.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HistorialEtapaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(HistorialEtapaDTO.class);
        HistorialEtapaDTO historialEtapaDTO1 = new HistorialEtapaDTO();
        historialEtapaDTO1.setId(1L);
        HistorialEtapaDTO historialEtapaDTO2 = new HistorialEtapaDTO();
        assertThat(historialEtapaDTO1).isNotEqualTo(historialEtapaDTO2);
        historialEtapaDTO2.setId(historialEtapaDTO1.getId());
        assertThat(historialEtapaDTO1).isEqualTo(historialEtapaDTO2);
        historialEtapaDTO2.setId(2L);
        assertThat(historialEtapaDTO1).isNotEqualTo(historialEtapaDTO2);
        historialEtapaDTO1.setId(null);
        assertThat(historialEtapaDTO1).isNotEqualTo(historialEtapaDTO2);
    }
}
