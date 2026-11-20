package com.mdd.booktor.controllers.web;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.services.MateriaService;
import com.mdd.booktor.services.ReservaService;
import com.mdd.booktor.services.TurnoService;

import jakarta.validation.Valid;

/**
 * Pantallas del tutor ({@code /tutor}): generar turnos y ver, dar de baja o cancelar los suyos.
 * El tutor se toma del token ({@link java.security.Principal}).
 */
@Controller
@RequestMapping("/tutor")
public class PanelTutorController {

    private final TurnoService turnoService;
    private final MateriaService materiaService;
    private final ReservaService reservaService;

    public PanelTutorController(TurnoService turnoService, MateriaService materiaService, ReservaService reservaService) {
        this.turnoService = turnoService;
        this.materiaService = materiaService;
        this.reservaService = reservaService;
    }

    /** Muestra el menú del tutor. */
    @GetMapping
    public String menu() {
        return "tutor/menu";
    }

    /** Muestra Mis turnos: los turnos futuros, con quién reservó y su objetivo. */
    @GetMapping("/turnos")
    public String misTurnos(Principal principal, Model model) {
        model.addAttribute("turnos", turnoService.listarProximosDeTutor(principal.getName()));
        return "tutor/turnos";
    }

    /** Muestra el formulario para generar un bloque de turnos. */
    @GetMapping("/turnos/nuevo")
    public String nuevoBloque(Model model) {
        model.addAttribute("materias", materiaService.listar());
        return "tutor/nuevo-bloque";
    }

    /**
     * Genera los turnos del bloque y vuelve a Mis turnos.
     * {@code @ModelAttribute} arma el DTO con los campos del formulario, como {@code @RequestBody} lo hace con el JSON.
     */
    @PostMapping("/turnos")
    public String generarBloque(@Valid @ModelAttribute BloqueTurnosRequest request, Principal principal) {
        turnoService.generarBloque(request, principal.getName());
        return "redirect:/tutor/turnos";
    }

    /** Da de baja un turno disponible y vuelve a Mis turnos. */
    @PostMapping("/turnos/baja")
    public String darDeBaja(@RequestParam Long turnoId, Principal principal) {
        turnoService.darDeBaja(turnoId, principal.getName());
        return "redirect:/tutor/turnos";
    }

    /** Cancela la reserva de uno de sus turnos y vuelve a Mis turnos. */
    @PostMapping("/reservas/cancelar")
    public String cancelarReserva(@RequestParam Long reservaId, Principal principal) {
        reservaService.cancelar(reservaId, principal.getName());
        return "redirect:/tutor/turnos";
    }
}
