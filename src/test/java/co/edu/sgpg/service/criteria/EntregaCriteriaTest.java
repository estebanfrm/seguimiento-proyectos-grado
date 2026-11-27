package co.edu.sgpg.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class EntregaCriteriaTest {

    @Test
    void newEntregaCriteriaHasAllFiltersNullTest() {
        var entregaCriteria = new EntregaCriteria();
        assertThat(entregaCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void entregaCriteriaFluentMethodsCreatesFiltersTest() {
        var entregaCriteria = new EntregaCriteria();

        setAllFilters(entregaCriteria);

        assertThat(entregaCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void entregaCriteriaCopyCreatesNullFilterTest() {
        var entregaCriteria = new EntregaCriteria();
        var copy = entregaCriteria.copy();

        assertThat(entregaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(entregaCriteria)
        );
    }

    @Test
    void entregaCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var entregaCriteria = new EntregaCriteria();
        setAllFilters(entregaCriteria);

        var copy = entregaCriteria.copy();

        assertThat(entregaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(entregaCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var entregaCriteria = new EntregaCriteria();

        assertThat(entregaCriteria).hasToString("EntregaCriteria{}");
    }

    private static void setAllFilters(EntregaCriteria entregaCriteria) {
        entregaCriteria.id();
        entregaCriteria.etapa();
        entregaCriteria.titulo();
        entregaCriteria.urlDocumento();
        entregaCriteria.version();
        entregaCriteria.fechaEntrega();
        entregaCriteria.estado();
        entregaCriteria.observacionId();
        entregaCriteria.autorId();
        entregaCriteria.proyectoId();
        entregaCriteria.distinct();
    }

    private static Condition<EntregaCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getEtapa()) &&
                condition.apply(criteria.getTitulo()) &&
                condition.apply(criteria.getUrlDocumento()) &&
                condition.apply(criteria.getVersion()) &&
                condition.apply(criteria.getFechaEntrega()) &&
                condition.apply(criteria.getEstado()) &&
                condition.apply(criteria.getObservacionId()) &&
                condition.apply(criteria.getAutorId()) &&
                condition.apply(criteria.getProyectoId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<EntregaCriteria> copyFiltersAre(EntregaCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getEtapa(), copy.getEtapa()) &&
                condition.apply(criteria.getTitulo(), copy.getTitulo()) &&
                condition.apply(criteria.getUrlDocumento(), copy.getUrlDocumento()) &&
                condition.apply(criteria.getVersion(), copy.getVersion()) &&
                condition.apply(criteria.getFechaEntrega(), copy.getFechaEntrega()) &&
                condition.apply(criteria.getEstado(), copy.getEstado()) &&
                condition.apply(criteria.getObservacionId(), copy.getObservacionId()) &&
                condition.apply(criteria.getAutorId(), copy.getAutorId()) &&
                condition.apply(criteria.getProyectoId(), copy.getProyectoId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
