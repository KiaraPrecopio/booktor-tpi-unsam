package com.mdd.booktor.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.mdd.booktor.models.Rol;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class InicioController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Después de iniciar sesión, cada usuario va al menú de su rol.
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
