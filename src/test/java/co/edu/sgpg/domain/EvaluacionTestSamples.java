package co.edu.sgpg.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class EvaluacionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Evaluacion getEvaluacionSample1() {
        return new Evaluacion().id(1L);
    }

    public static Evaluacion getEvaluacionSample2() {
        return new Evaluacion().id(2L);
    }

    public static Evaluacion getEvaluacionRandomSampleGenerator() {
        return new Evaluacion().id(longCount.incrementAndGet());
    }
}
