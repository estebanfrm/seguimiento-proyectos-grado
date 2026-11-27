package co.edu.sgpg.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class FechaLimiteEtapaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static FechaLimiteEtapa getFechaLimiteEtapaSample1() {
        return new FechaLimiteEtapa().id(1L).descripcion("descripcion1");
    }

    public static FechaLimiteEtapa getFechaLimiteEtapaSample2() {
        return new FechaLimiteEtapa().id(2L).descripcion("descripcion2");
    }

    public static FechaLimiteEtapa getFechaLimiteEtapaRandomSampleGenerator() {
        return new FechaLimiteEtapa().id(longCount.incrementAndGet()).descripcion(UUID.randomUUID().toString());
    }
}
