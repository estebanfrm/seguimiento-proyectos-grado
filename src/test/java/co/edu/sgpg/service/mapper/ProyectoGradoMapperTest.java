package co.edu.sgpg.service.mapper;

import static co.edu.sgpg.domain.ProyectoGradoAsserts.*;
import static co.edu.sgpg.domain.ProyectoGradoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProyectoGradoMapperTest {

    private ProyectoGradoMapper proyectoGradoMapper;

    @BeforeEach
    void setUp() {
        proyectoGradoMapper = new ProyectoGradoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getProyectoGradoSample1();
        var actual = proyectoGradoMapper.toEntity(proyectoGradoMapper.toDto(expected));
        assertProyectoGradoAllPropertiesEquals(expected, actual);
    }
}
