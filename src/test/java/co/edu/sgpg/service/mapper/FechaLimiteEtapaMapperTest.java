package co.edu.sgpg.service.mapper;

import static co.edu.sgpg.domain.FechaLimiteEtapaAsserts.*;
import static co.edu.sgpg.domain.FechaLimiteEtapaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FechaLimiteEtapaMapperTest {

    private FechaLimiteEtapaMapper fechaLimiteEtapaMapper;

    @BeforeEach
    void setUp() {
        fechaLimiteEtapaMapper = new FechaLimiteEtapaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getFechaLimiteEtapaSample1();
        var actual = fechaLimiteEtapaMapper.toEntity(fechaLimiteEtapaMapper.toDto(expected));
        assertFechaLimiteEtapaAllPropertiesEquals(expected, actual);
    }
}
