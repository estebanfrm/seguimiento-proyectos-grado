package co.edu.sgpg.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ObservacionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Observacion getObservacionSample1() {
        return new Observacion().id(1L);
    }

    public static Observacion getObservacionSample2() {
        return new Observacion().id(2L);
    }

    public static Observacion getObservacionRandomSampleGenerator() {
        return new Observacion().id(longCount.incrementAndGet());
    }
}
