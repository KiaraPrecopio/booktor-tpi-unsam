package com.mdd.booktor.controllers.web;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.services.AdaptacionService;
import com.mdd.booktor.services.BusquedaService;
import com.mdd.booktor.services.MateriaService;
import com.mdd.booktor.services.ReservaService;

import jakarta.validation.Valid;

/**
 * Pantallas del estudiante ({@code /estudiante}): buscar tutoría, reservar y ver o cancelar sus reservas.
 * El estudiante se toma del token ({@link java.security.Principal}).
 */
@Controller
@RequestMapping("/estudiante")
public class PanelEstudianteController {

    private final BusquedaService busquedaService;
    private final MateriaService materiaService;
    private final AdaptacionService adaptacionService;
    private final ReservaService reservaService;

    public PanelEstudianteController(BusquedaService busquedaService, MateriaService materiaService,
            AdaptacionService adaptacionService, ReservaService reservaService) {
        this.busquedaService = busquedaService;
        this.materiaService = materiaService;
        this.adaptacionService = adaptacionService;
        this.reservaService = reservaService;
    }

    /** Muestra el menú del estudiante. */
    @GetMapping
    public String menu() {
        return "estudiante/menu";
    }

    /** Busca materias con turnos disponibles por parte del nombre. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String nombre, Model model) {
        model.addAttribute("materias", busquedaService.buscarMaterias(nombre));
        model.addAttribute("nombre", nombre);
        return "estudiante/buscar";
    }

    /** Muestra los tutores con turnos disponibles de una materia, filtrados por adaptaciones y modalidad. */
    @GetMapping("/tutores")
    public String tutores(@RequestParam Long materiaId, @RequestParam(required = false) Set<Long> adaptaciones,
            @RequestParam(required = false) Modalidad modalidad, Model model) {
        model.addAttribute("materia", materiaService.buscarPorId(materiaId));
        model.addAttribute("adaptaciones", adaptacionService.listar());
        model.addAttribute("tutores", busquedaService.buscarTutores(materiaId, adaptaciones, modalidad));
        // Para dejar marcados los filtros que eligió.
        model.addAttribute("seleccionadas", adaptaciones == null ? new HashSet<Long>() : adaptaciones);
        model.addAttribute("modalidad", modalidad == null ? "" : modalidad.name());
        return "estudiante/tutores";
    }

    /** Reserva un turno con el objetivo opcional y va a Mis reservas. */
    @PostMapping("/reservas")
    public String reservar(@Valid @ModelAttribute ReservaRequest request, Principal principal) {
        reservaService.reservar(request, principal.getName());
        return "redirect:/estudiante/reservas";
    }

    /** Muestra Mis reservas: las reservas activas y futuras, con el link o el lugar. */
    @GetMapping("/reservas")
    public String misReservas(Principal principal, Model model) {
        model.addAttribute("reservas", reservaService.listarProximasDeEstudiante(principal.getName()));
        return "estudiante/reservas";
    }

    /** Cancela una reserva propia y vuelve a Mis reservas. */
    @PostMapping("/reservas/cancelar")
    public String cancelar(@RequestParam Long reservaId, Principal principal) {
        reservaService.cancelar(reservaId, principal.getName());
        return "redirect:/estudiante/reservas";
    }
}
