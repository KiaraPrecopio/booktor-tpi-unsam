package com.mdd.booktor.configs;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.util.WebUtils;

import com.mdd.booktor.models.Rol;
import com.mdd.booktor.services.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = Rol.ADMINISTRADOR.name();
    private static final String TUTOR = Rol.TUTOR.name();
    private static final String ESTUDIANTE = Rol.ESTUDIANTE.name();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/api/auth/login", "/error", "/webjars/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/materias/**", "/api/adaptaciones/**").authenticated()
                .requestMatchers("/api/materias/**", "/api/adaptaciones/**").hasRole(ADMIN)
                .requestMatchers("/api/estudiantes/**", "/api/tutores/**").hasRole(ADMIN)
                .requestMatchers("/api/turnos/**").hasRole(TUTOR)
                .requestMatchers("/api/busqueda/**").hasRole(ESTUDIANTE)
                .requestMatchers(HttpMethod.POST, "/api/reservas").hasRole(ESTUDIANTE)
                .requestMatchers("/api/reservas/mis-reservas").hasRole(ESTUDIANTE)
                .requestMatchers("/api/reservas/activas").hasRole(ADMIN)
                .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/cancelacion").hasAnyRole(ADMIN, TUTOR, ESTUDIANTE)
                .requestMatchers("/admin/**").hasRole(ADMIN)
                .requestMatchers("/tutor/**").hasRole(TUTOR)
                .requestMatchers("/estudiante/**").hasRole(ESTUDIANTE)
                .anyRequest().authenticated())

            // No hay sesión en el servidor: cada pedido trae su token.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(this::leerToken)
                .jwt(jwt -> jwt.jwtAuthenticationConverter(rolesDelToken()))
                .authenticationEntryPoint(this::sinAutenticar))
            .exceptionHandling(errores -> errores
                .authenticationEntryPoint(this::sinAutenticar)
                .accessDeniedHandler(this::sinPermiso))

            .logout(logout -> logout
                .deleteCookies(JwtService.COOKIE)
                .logoutSuccessUrl("/login?logout"));

        return http.build();
    }

    // El que valida email y contraseña (con UsuarioDetailsServiceImpl y BCrypt).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // El claim "rol" ya trae ROLE_TUTOR, etc.: así siguen funcionando hasRole e isUserInRole.
    private JwtAuthenticationConverter rolesDelToken() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    // Postman y Swagger mandan el token en el encabezado Authorization; el navegador, en la cookie.
    private String leerToken(HttpServletRequest request) {
        String token = new DefaultBearerTokenResolver().resolve(request);
        Cookie cookie = WebUtils.getCookie(request, JwtService.COOKIE);
        if (token == null && cookie != null) {
            token = cookie.getValue();
        }
        return token;
    }

    // Sin token o con uno vencido: la API responde 401 y las pantallas borran la cookie y vuelven al login.
    private void sinAutenticar(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException ex) throws IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Cookie vencida = new Cookie(JwtService.COOKIE, null);
        vencida.setPath("/");
        vencida.setMaxAge(0);
        response.addCookie(vencida);
        response.sendRedirect("/login");
    }

    // Con token, pero de otro rol: 403. sendError hace que las pantallas muestren error.html.
    private void sinPermiso(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException ex) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tenés permiso para entrar a esta página.");
    }
}
