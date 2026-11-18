package com.mdd.booktor.controllers.web;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.dtos.EstudianteRequest;
import com.mdd.booktor.dtos.EstudianteResponse;
import com.mdd.booktor.services.AdaptacionService;
import com.mdd.booktor.services.EstudianteService;

import jakarta.validation.Valid;

// ABML de estudiantes para el administrador.
@Controller
@RequestMapping("/admin/estudiantes")
public class EstudianteWebController {

    private final EstudianteService estudianteService;
    private final AdaptacionService adaptacionService;

    public EstudianteWebController(EstudianteService estudianteService, AdaptacionService adaptacionService) {
        this.estudianteService = estudianteService;
        this.adaptacionService = adaptacionService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("estudiantes", estudianteService.listar());
        model.addAttribute("adaptaciones", adaptacionService.listar());
        return "admin/estudiantes";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute EstudianteRequest request) {
        estudianteService.crear(request);
        return "redirect:/admin/estudiantes";
    }

    @GetMapping("/editar")
    public String editar(@RequestParam Long id, Model model) {
        EstudianteResponse estudiante = estudianteService.buscarPorId(id);
        // Para dejar marcadas las adaptaciones que ya tiene.
        Set<Long> seleccionadas = new HashSet<>();
        for (AdaptacionResponse adaptacion : estudiante.adaptaciones()) {
            seleccionadas.add(adaptacion.id());
        }
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("adaptaciones", adaptacionService.listar());
        model.addAttribute("seleccionadas", seleccionadas);
        return "admin/estudiante-editar";
    }

    @PostMapping("/modificar")
    public String modificar(@RequestParam Long id, @Valid @ModelAttribute EstudianteRequest request) {
        estudianteService.modificar(id, request);
        return "redirect:/admin/estudiantes";
    }

    @PostMapping("/baja")
    public String darDeBaja(@RequestParam Long id) {
        estudianteService.darDeBaja(id);
        return "redirect:/admin/estudiantes";
    }
}
