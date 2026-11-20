package com.mdd.booktor.configs;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Define el reloj que usa toda la aplicación, con la hora de Argentina.
 * Los services lo reciben por constructor en lugar de llamar a {@code LocalDateTime.now()},
 * así en los tests se puede reemplazar por un reloj fijo.
 */
@Configuration
public class RelojConfig {

    /**
     * Reloj del sistema en la zona horaria de Buenos Aires.
     *
     * @return el reloj de la aplicación
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Argentina/Buenos_Aires"));
    }
    
}
