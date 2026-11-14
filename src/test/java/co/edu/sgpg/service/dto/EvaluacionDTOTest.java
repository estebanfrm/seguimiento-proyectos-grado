package co.edu.sgpg.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EvaluacionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EvaluacionDTO.class);
        EvaluacionDTO evaluacionDTO1 = new EvaluacionDTO();
        evaluacionDTO1.setId(1L);
        EvaluacionDTO evaluacionDTO2 = new EvaluacionDTO();
        assertThat(evaluacionDTO1).isNotEqualTo(evaluacionDTO2);
        evaluacionDTO2.setId(evaluacionDTO1.getId());
        assertThat(evaluacionDTO1).isEqualTo(evaluacionDTO2);
        evaluacionDTO2.setId(2L);
        assertThat(evaluacionDTO1).isNotEqualTo(evaluacionDTO2);
        evaluacionDTO1.setId(null);
        assertThat(evaluacionDTO1).isNotEqualTo(evaluacionDTO2);
    }
}
