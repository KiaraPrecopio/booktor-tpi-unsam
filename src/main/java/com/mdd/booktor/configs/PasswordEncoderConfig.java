package com.mdd.booktor.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Define cómo se encriptan las contraseñas antes de guardarlas.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt con fuerza 12: un hash lento y con sal, para que no se puedan recuperar las contraseñas.
     *
     * @return el encriptador de contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
