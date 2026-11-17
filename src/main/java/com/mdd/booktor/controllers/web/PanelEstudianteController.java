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

// Pantallas del estudiante: buscar tutoría, reservar y ver o cancelar sus reservas.
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

    @GetMapping
    public String menu() {
        return "estudiante/menu";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String nombre, Model model) {
        model.addAttribute("materias", busquedaService.buscarMaterias(nombre));
        model.addAttribute("nombre", nombre);
        return "estudiante/buscar";
    }

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

    @PostMapping("/reservas")
    public String reservar(@Valid @ModelAttribute ReservaRequest request, Principal principal) {
        reservaService.reservar(request, principal.getName());
        return "redirect:/estudiante/reservas";
    }

    @GetMapping("/reservas")
    public String misReservas(Principal principal, Model model) {
        model.addAttribute("reservas", reservaService.listarProximasDeEstudiante(principal.getName()));
        return "estudiante/reservas";
    }

    @PostMapping("/reservas/cancelar")
    public String cancelar(@RequestParam Long reservaId, Principal principal) {
        reservaService.cancelar(reservaId, principal.getName());
        return "redirect:/estudiante/reservas";
    }
}
