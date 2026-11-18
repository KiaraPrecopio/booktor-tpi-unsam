package com.mdd.booktor.services;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    // Nombre de la cookie donde el navegador guarda el token.
    public static final String COOKIE = "token";

    private final JwtEncoder jwtEncoder;
    private final Clock reloj;

    public JwtService(JwtEncoder jwtEncoder, Clock reloj) {
        this.jwtEncoder = jwtEncoder;
        this.reloj = reloj;
    }

    // El token lleva el email (subject) y el rol del usuario (ROLE_TUTOR, ...). Vence en una hora.
    public String generarToken(Authentication autenticacion) {
        Instant ahora = reloj.instant();
        String rol = autenticacion.getAuthorities().iterator().next().getAuthority();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("booktor")
                .subject(autenticacion.getName())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(1, ChronoUnit.HOURS))
                .claim("rol", rol)
                .build();

        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(encabezado, claims)).getTokenValue();
    }
}
