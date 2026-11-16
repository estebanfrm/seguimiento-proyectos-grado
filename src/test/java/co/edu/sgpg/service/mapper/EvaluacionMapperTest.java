package co.edu.sgpg.service.mapper;

import static co.edu.sgpg.domain.EvaluacionAsserts.*;
import static co.edu.sgpg.domain.EvaluacionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EvaluacionMapperTest {

    private EvaluacionMapper evaluacionMapper;

    @BeforeEach
    void setUp() {
        evaluacionMapper = new EvaluacionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEvaluacionSample1();
        var actual = evaluacionMapper.toEntity(evaluacionMapper.toDto(expected));
        assertEvaluacionAllPropertiesEquals(expected, actual);
    }
}
