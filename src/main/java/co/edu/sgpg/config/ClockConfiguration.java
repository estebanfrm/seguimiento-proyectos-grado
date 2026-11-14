package co.edu.sgpg.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj inyectable: toda regla que dependa de "ahora" (fechas límite, fechas forzadas) debe usar
 * {@code Instant.now(clock)} para poder probarse con un reloj fijo.
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
