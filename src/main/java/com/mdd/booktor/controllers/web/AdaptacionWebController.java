package com.mdd.booktor.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mdd.booktor.dtos.AdaptacionRequest;
import com.mdd.booktor.services.AdaptacionService;

import jakarta.validation.Valid;

// ABML de adaptaciones para el administrador.
@Controller
@RequestMapping("/admin/adaptaciones")
public class AdaptacionWebController {

    private final AdaptacionService adaptacionService;

    public AdaptacionWebController(AdaptacionService adaptacionService) {
        this.adaptacionService = adaptacionService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("adaptaciones", adaptacionService.listar());
        return "admin/adaptaciones";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute AdaptacionRequest request) {
        adaptacionService.crear(request);
        return "redirect:/admin/adaptaciones";
    }

    @GetMapping("/editar")
    public String editar(@RequestParam Long id, Model model) {
        model.addAttribute("adaptacion", adaptacionService.buscarPorId(id));
        return "admin/adaptacion-editar";
    }

    @PostMapping("/modificar")
    public String modificar(@RequestParam Long id, @Valid @ModelAttribute AdaptacionRequest request) {
        adaptacionService.modificar(id, request);
        return "redirect:/admin/adaptaciones";
    }

    @PostMapping("/baja")
    public String darDeBaja(@RequestParam Long id) {
        adaptacionService.darDeBaja(id);
        return "redirect:/admin/adaptaciones";
    }
}
