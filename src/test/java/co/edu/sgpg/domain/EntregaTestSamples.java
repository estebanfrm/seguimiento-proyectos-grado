package co.edu.sgpg.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EntregaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Entrega getEntregaSample1() {
        return new Entrega().id(1L).titulo("titulo1").urlDocumento("urlDocumento1").version(1);
    }

    public static Entrega getEntregaSample2() {
        return new Entrega().id(2L).titulo("titulo2").urlDocumento("urlDocumento2").version(2);
    }

    public static Entrega getEntregaRandomSampleGenerator() {
        return new Entrega()
            .id(longCount.incrementAndGet())
            .titulo(UUID.randomUUID().toString())
            .urlDocumento(UUID.randomUUID().toString())
            .version(intCount.incrementAndGet());
    }
}
