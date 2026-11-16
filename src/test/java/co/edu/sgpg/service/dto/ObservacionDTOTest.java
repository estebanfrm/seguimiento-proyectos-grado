package co.edu.sgpg.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ObservacionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ObservacionDTO.class);
        ObservacionDTO observacionDTO1 = new ObservacionDTO();
        observacionDTO1.setId(1L);
        ObservacionDTO observacionDTO2 = new ObservacionDTO();
        assertThat(observacionDTO1).isNotEqualTo(observacionDTO2);
        observacionDTO2.setId(observacionDTO1.getId());
        assertThat(observacionDTO1).isEqualTo(observacionDTO2);
        observacionDTO2.setId(2L);
        assertThat(observacionDTO1).isNotEqualTo(observacionDTO2);
        observacionDTO1.setId(null);
        assertThat(observacionDTO1).isNotEqualTo(observacionDTO2);
    }
}
