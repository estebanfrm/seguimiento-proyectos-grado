package co.edu.sgpg.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ProyectoGradoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static ProyectoGrado getProyectoGradoSample1() {
        return new ProyectoGrado().id(1L).titulo("titulo1").programa("programa1").porcentajeAvance(1);
    }

    public static ProyectoGrado getProyectoGradoSample2() {
        return new ProyectoGrado().id(2L).titulo("titulo2").programa("programa2").porcentajeAvance(2);
    }

    public static ProyectoGrado getProyectoGradoRandomSampleGenerator() {
        return new ProyectoGrado()
            .id(longCount.incrementAndGet())
            .titulo(UUID.randomUUID().toString())
            .programa(UUID.randomUUID().toString())
            .porcentajeAvance(intCount.incrementAndGet());
    }
}
