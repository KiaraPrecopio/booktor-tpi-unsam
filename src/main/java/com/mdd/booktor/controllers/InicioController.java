package com.mdd.booktor.controllers;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mdd.booktor.models.Rol;
import com.mdd.booktor.services.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Pantallas de inicio de sesión y redirección al menú de cada rol. */
@Controller
public class InicioController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public InicioController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Muestra el formulario de inicio de sesión. */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Valida el email y la contraseña y guarda el token JWT en una cookie HttpOnly y SameSite=Strict,
     * para que el navegador lo mande solo. Es el mismo token que devuelve {@code /api/auth/login}.
     * Si las credenciales son incorrectas, vuelve al login con un aviso.
     */
    @PostMapping("/login")
    public String iniciarSesion(@RequestParam String email, @RequestParam String password,
            HttpServletResponse response) {
        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            Cookie cookie = new Cookie(JwtService.COOKIE, jwtService.generarToken(autenticacion));
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setAttribute("SameSite", "Strict");
            response.addCookie(cookie);
            return "redirect:/";
        } catch (AuthenticationException ex) {
            return "redirect:/login?error";
        }
    }

    /** Después de iniciar sesión, manda a cada usuario al menú de su rol. */
    @GetMapping("/")
    public String inicio(HttpServletRequest request) {
        if (request.isUserInRole(Rol.ADMINISTRADOR.name())) {
            return "redirect:/admin";
        }
        if (request.isUserInRole(Rol.TUTOR.name())) {
            return "redirect:/tutor";
        }
        return "redirect:/estudiante";
    }
}
