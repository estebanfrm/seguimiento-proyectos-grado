package co.edu.sgpg.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProyectoGradoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProyectoGradoDTO.class);
        ProyectoGradoDTO proyectoGradoDTO1 = new ProyectoGradoDTO();
        proyectoGradoDTO1.setId(1L);
        ProyectoGradoDTO proyectoGradoDTO2 = new ProyectoGradoDTO();
        assertThat(proyectoGradoDTO1).isNotEqualTo(proyectoGradoDTO2);
        proyectoGradoDTO2.setId(proyectoGradoDTO1.getId());
        assertThat(proyectoGradoDTO1).isEqualTo(proyectoGradoDTO2);
        proyectoGradoDTO2.setId(2L);
        assertThat(proyectoGradoDTO1).isNotEqualTo(proyectoGradoDTO2);
        proyectoGradoDTO1.setId(null);
        assertThat(proyectoGradoDTO1).isNotEqualTo(proyectoGradoDTO2);
    }
}
