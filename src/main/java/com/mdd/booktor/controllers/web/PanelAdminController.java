package com.mdd.booktor.controllers.web;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mdd.booktor.services.ReservaService;

/**
 * Menú del administrador y gestión de las reservas activas ({@code /admin}).
 * Los ABML están en un controller por entidad.
 */
@Controller
@RequestMapping("/admin")
public class PanelAdminController {

    private final ReservaService reservaService;

    public PanelAdminController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /** Muestra el menú del administrador. */
    @GetMapping
    public String menu() {
        return "admin/menu";
    }

    /** Muestra todas las reservas activas de turnos que no empezaron. */
    @GetMapping("/reservas")
    public String reservasActivas(Model model) {
        model.addAttribute("reservas", reservaService.listarActivasFuturas());
        return "admin/reservas";
    }

    /** Cancela una reserva y vuelve a la lista. */
    @PostMapping("/reservas/cancelar")
    public String cancelar(@RequestParam Long reservaId, Principal principal) {
        reservaService.cancelar(reservaId, principal.getName());
        return "redirect:/admin/reservas";
    }
}
