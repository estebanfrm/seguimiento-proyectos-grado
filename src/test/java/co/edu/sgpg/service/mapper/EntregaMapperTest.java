package co.edu.sgpg.service.mapper;

import static co.edu.sgpg.domain.EntregaAsserts.*;
import static co.edu.sgpg.domain.EntregaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EntregaMapperTest {

    private EntregaMapper entregaMapper;

    @BeforeEach
    void setUp() {
        entregaMapper = new EntregaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEntregaSample1();
        var actual = entregaMapper.toEntity(entregaMapper.toDto(expected));
        assertEntregaAllPropertiesEquals(expected, actual);
    }
}
