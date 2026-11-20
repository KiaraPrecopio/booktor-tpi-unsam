package com.mdd.booktor.configs;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Firma (encoder) y verifica (decoder) los tokens JWT con la misma clave secreta, usando HS256.
 * La clave viene de la variable {@code JWT_SECRET} del .env, nunca del repositorio.
 */
@Configuration
public class JwtConfig {

    @Value("${booktor.jwt.secret}")
    private String jwtSecret;

    private SecretKey crearClaveSecreta() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }


    /**
     * Verifica la firma y el vencimiento de los tokens que llegan en cada pedido.
     *
     * @return el decodificador de tokens
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(crearClaveSecreta())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Firma los tokens que genera {@link com.mdd.booktor.services.JwtService} al iniciar sesión.
     *
     * @return el codificador de tokens
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(crearClaveSecreta()));
    }
}
