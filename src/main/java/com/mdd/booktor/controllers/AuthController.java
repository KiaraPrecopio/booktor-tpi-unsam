package com.mdd.booktor.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdd.booktor.dtos.LoginRequest;
import com.mdd.booktor.dtos.LoginResponse;
import com.mdd.booktor.services.JwtService;

import jakarta.validation.Valid;

/**
 * API REST de inicio de sesión ({@code /api/auth}). Devuelve el token JWT que Postman y Swagger
 * mandan después en el header {@code Authorization: Bearer}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Valida el email y la contraseña con Spring Security y responde 200 con el token.
     * Si son incorrectos, {@link com.mdd.booktor.exceptions.GlobalExceptionHandler} responde 401.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        return ResponseEntity.ok(new LoginResponse(jwtService.generarToken(autenticacion), "Bearer"));
    }
}
