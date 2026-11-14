package co.edu.sgpg.service.mapper;

import static co.edu.sgpg.domain.ObservacionAsserts.*;
import static co.edu.sgpg.domain.ObservacionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObservacionMapperTest {

    private ObservacionMapper observacionMapper;

    @BeforeEach
    void setUp() {
        observacionMapper = new ObservacionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getObservacionSample1();
        var actual = observacionMapper.toEntity(observacionMapper.toDto(expected));
        assertObservacionAllPropertiesEquals(expected, actual);
    }
}
