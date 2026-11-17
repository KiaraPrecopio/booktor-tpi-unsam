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
import com.mdd.booktor.dtos.TutorRequest;
import com.mdd.booktor.dtos.TutorResponse;
import com.mdd.booktor.services.AdaptacionService;
import com.mdd.booktor.services.TutorService;

import jakarta.validation.Valid;

// ABML de tutores para el administrador.
@Controller
@RequestMapping("/admin/tutores")
public class TutorWebController {

    private final TutorService tutorService;
    private final AdaptacionService adaptacionService;

    public TutorWebController(TutorService tutorService, AdaptacionService adaptacionService) {
        this.tutorService = tutorService;
        this.adaptacionService = adaptacionService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tutores", tutorService.listar());
        model.addAttribute("adaptaciones", adaptacionService.listar());
        return "admin/tutores";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute TutorRequest request) {
        tutorService.crear(request);
        return "redirect:/admin/tutores";
    }

    @GetMapping("/editar")
    public String editar(@RequestParam Long id, Model model) {
        TutorResponse tutor = tutorService.buscarPorId(id);
        // Para dejar marcadas las adaptaciones que ya ofrece.
        Set<Long> seleccionadas = new HashSet<>();
        for (AdaptacionResponse adaptacion : tutor.adaptaciones()) {
            seleccionadas.add(adaptacion.id());
        }
        model.addAttribute("tutor", tutor);
        model.addAttribute("adaptaciones", adaptacionService.listar());
        model.addAttribute("seleccionadas", seleccionadas);
        return "admin/tutor-editar";
    }

    @PostMapping("/modificar")
    public String modificar(@RequestParam Long id, @Valid @ModelAttribute TutorRequest request) {
        tutorService.modificar(id, request);
        return "redirect:/admin/tutores";
    }

    @PostMapping("/baja")
    public String darDeBaja(@RequestParam Long id) {
        tutorService.darDeBaja(id);
        return "redirect:/admin/tutores";
    }
}
