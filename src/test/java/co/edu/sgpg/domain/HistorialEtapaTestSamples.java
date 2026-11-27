package co.edu.sgpg.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class HistorialEtapaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static HistorialEtapa getHistorialEtapaSample1() {
        return new HistorialEtapa().id(1L).comentario("comentario1");
    }

    public static HistorialEtapa getHistorialEtapaSample2() {
        return new HistorialEtapa().id(2L).comentario("comentario2");
    }

    public static HistorialEtapa getHistorialEtapaRandomSampleGenerator() {
        return new HistorialEtapa().id(longCount.incrementAndGet()).comentario(UUID.randomUUID().toString());
    }
}
