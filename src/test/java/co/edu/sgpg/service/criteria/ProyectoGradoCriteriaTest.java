package co.edu.sgpg.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ProyectoGradoCriteriaTest {

    @Test
    void newProyectoGradoCriteriaHasAllFiltersNullTest() {
        var proyectoGradoCriteria = new ProyectoGradoCriteria();
        assertThat(proyectoGradoCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void proyectoGradoCriteriaFluentMethodsCreatesFiltersTest() {
        var proyectoGradoCriteria = new ProyectoGradoCriteria();

        setAllFilters(proyectoGradoCriteria);

        assertThat(proyectoGradoCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void proyectoGradoCriteriaCopyCreatesNullFilterTest() {
        var proyectoGradoCriteria = new ProyectoGradoCriteria();
        var copy = proyectoGradoCriteria.copy();

        assertThat(proyectoGradoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(proyectoGradoCriteria)
        );
    }

    @Test
    void proyectoGradoCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var proyectoGradoCriteria = new ProyectoGradoCriteria();
        setAllFilters(proyectoGradoCriteria);

        var copy = proyectoGradoCriteria.copy();

        assertThat(proyectoGradoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(proyectoGradoCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var proyectoGradoCriteria = new ProyectoGradoCriteria();

        assertThat(proyectoGradoCriteria).hasToString("ProyectoGradoCriteria{}");
    }

    private static void setAllFilters(ProyectoGradoCriteria proyectoGradoCriteria) {
        proyectoGradoCriteria.id();
        proyectoGradoCriteria.titulo();
        proyectoGradoCriteria.programa();
        proyectoGradoCriteria.etapaActual();
        proyectoGradoCriteria.estado();
        proyectoGradoCriteria.porcentajeAvance();
        proyectoGradoCriteria.fechaRegistro();
        proyectoGradoCriteria.fechaActualizacion();
        proyectoGradoCriteria.entregaId();
        proyectoGradoCriteria.fechaLimiteId();
        proyectoGradoCriteria.evaluacionId();
        proyectoGradoCriteria.historialId();
        proyectoGradoCriteria.tutorId();
        proyectoGradoCriteria.estudianteId();
        proyectoGradoCriteria.juradoId();
        proyectoGradoCriteria.distinct();
    }

    private static Condition<ProyectoGradoCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitulo()) &&
                condition.apply(criteria.getPrograma()) &&
                condition.apply(criteria.getEtapaActual()) &&
                condition.apply(criteria.getEstado()) &&
                condition.apply(criteria.getPorcentajeAvance()) &&
                condition.apply(criteria.getFechaRegistro()) &&
                condition.apply(criteria.getFechaActualizacion()) &&
                condition.apply(criteria.getEntregaId()) &&
                condition.apply(criteria.getFechaLimiteId()) &&
                condition.apply(criteria.getEvaluacionId()) &&
                condition.apply(criteria.getHistorialId()) &&
                condition.apply(criteria.getTutorId()) &&
                condition.apply(criteria.getEstudianteId()) &&
                condition.apply(criteria.getJuradoId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ProyectoGradoCriteria> copyFiltersAre(
        ProyectoGradoCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitulo(), copy.getTitulo()) &&
                condition.apply(criteria.getPrograma(), copy.getPrograma()) &&
                condition.apply(criteria.getEtapaActual(), copy.getEtapaActual()) &&
                condition.apply(criteria.getEstado(), copy.getEstado()) &&
                condition.apply(criteria.getPorcentajeAvance(), copy.getPorcentajeAvance()) &&
                condition.apply(criteria.getFechaRegistro(), copy.getFechaRegistro()) &&
                condition.apply(criteria.getFechaActualizacion(), copy.getFechaActualizacion()) &&
                condition.apply(criteria.getEntregaId(), copy.getEntregaId()) &&
                condition.apply(criteria.getFechaLimiteId(), copy.getFechaLimiteId()) &&
                condition.apply(criteria.getEvaluacionId(), copy.getEvaluacionId()) &&
                condition.apply(criteria.getHistorialId(), copy.getHistorialId()) &&
                condition.apply(criteria.getTutorId(), copy.getTutorId()) &&
                condition.apply(criteria.getEstudianteId(), copy.getEstudianteId()) &&
                condition.apply(criteria.getJuradoId(), copy.getJuradoId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
