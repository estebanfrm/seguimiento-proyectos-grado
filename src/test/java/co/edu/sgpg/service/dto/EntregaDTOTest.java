package co.edu.sgpg.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EntregaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EntregaDTO.class);
        EntregaDTO entregaDTO1 = new EntregaDTO();
        entregaDTO1.setId(1L);
        EntregaDTO entregaDTO2 = new EntregaDTO();
        assertThat(entregaDTO1).isNotEqualTo(entregaDTO2);
        entregaDTO2.setId(entregaDTO1.getId());
        assertThat(entregaDTO1).isEqualTo(entregaDTO2);
        entregaDTO2.setId(2L);
        assertThat(entregaDTO1).isNotEqualTo(entregaDTO2);
        entregaDTO1.setId(null);
        assertThat(entregaDTO1).isNotEqualTo(entregaDTO2);
    }
}
