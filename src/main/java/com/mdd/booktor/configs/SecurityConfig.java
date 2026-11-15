package com.mdd.booktor.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.mdd.booktor.models.Rol;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = Rol.ADMINISTRADOR.name();
    private static final String TUTOR = Rol.TUTOR.name();
    private static final String ESTUDIANTE = Rol.ESTUDIANTE.name();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/materias/**", "/api/adaptaciones/**").authenticated()
                .requestMatchers("/api/materias/**", "/api/adaptaciones/**").hasRole(ADMIN)
                .requestMatchers("/api/estudiantes/**", "/api/tutores/**").hasRole(ADMIN)
                .requestMatchers("/api/turnos/**").hasRole(TUTOR)
                .requestMatchers("/api/busqueda/**").hasRole(ESTUDIANTE)
                .requestMatchers(HttpMethod.POST, "/api/reservas").hasRole(ESTUDIANTE)
                .requestMatchers("/api/reservas/estudiante/**").hasRole(ESTUDIANTE)
                .requestMatchers("/api/reservas/activas").hasRole(ADMIN)
                .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/cancelacion").hasAnyRole(ADMIN, TUTOR, ESTUDIANTE)
                .anyRequest().authenticated())

            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/", true)
                .permitAll())

            .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
            .exceptionHandling(errores -> errores.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                PathPatternRequestMatcher.pathPattern("/api/**")))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }
}
