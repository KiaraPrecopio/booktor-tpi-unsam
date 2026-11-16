package com.mdd.booktor.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mdd.booktor.dtos.MateriaRequest;
import com.mdd.booktor.services.MateriaService;

import jakarta.validation.Valid;

// ABML de materias para el administrador.
@Controller
@RequestMapping("/admin/materias")
public class MateriaWebController {

    private final MateriaService materiaService;

    public MateriaWebController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("materias", materiaService.listar());
        return "admin/materias";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute MateriaRequest request) {
        materiaService.crear(request);
        return "redirect:/admin/materias";
    }

    @GetMapping("/editar")
    public String editar(@RequestParam Long id, Model model) {
        model.addAttribute("materia", materiaService.buscarPorId(id));
        return "admin/materia-editar";
    }

    @PostMapping("/modificar")
    public String modificar(@RequestParam Long id, @Valid @ModelAttribute MateriaRequest request) {
        materiaService.modificar(id, request);
        return "redirect:/admin/materias";
    }

    @PostMapping("/baja")
    public String darDeBaja(@RequestParam Long id) {
        materiaService.darDeBaja(id);
        return "redirect:/admin/materias";
    }
}
