package co.edu.sgpg.service.mapper;

import static co.edu.sgpg.domain.HistorialEtapaAsserts.*;
import static co.edu.sgpg.domain.HistorialEtapaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HistorialEtapaMapperTest {

    private HistorialEtapaMapper historialEtapaMapper;

    @BeforeEach
    void setUp() {
        historialEtapaMapper = new HistorialEtapaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getHistorialEtapaSample1();
        var actual = historialEtapaMapper.toEntity(historialEtapaMapper.toDto(expected));
        assertHistorialEtapaAllPropertiesEquals(expected, actual);
    }
}
