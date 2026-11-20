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

/**
 * Pantallas del ABML de adaptaciones para el administrador ({@code /admin/adaptaciones}).
 * Usan el mismo service que la API; si el service rechaza una operación,
 * {@link WebControllerAdvice} muestra la página de error.
 */
@Controller
@RequestMapping("/admin/adaptaciones")
public class AdaptacionWebController {

    private final AdaptacionService adaptacionService;

    public AdaptacionWebController(AdaptacionService adaptacionService) {
        this.adaptacionService = adaptacionService;
    }

    /** Muestra la lista de adaptaciones y el formulario de alta. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("adaptaciones", adaptacionService.listar());
        return "admin/adaptaciones";
    }

    /** Da de alta una adaptación con los datos del formulario y vuelve a la lista. */
    @PostMapping
    public String crear(@Valid @ModelAttribute AdaptacionRequest request) {
        adaptacionService.crear(request);
        return "redirect:/admin/adaptaciones";
    }

    /** Muestra el formulario de edición. */
    @GetMapping("/editar")
    public String editar(@RequestParam Long id, Model model) {
        model.addAttribute("adaptacion", adaptacionService.buscarPorId(id));
        return "admin/adaptacion-editar";
    }

    /** Guarda los cambios y vuelve a la lista. */
    @PostMapping("/modificar")
    public String modificar(@RequestParam Long id, @Valid @ModelAttribute AdaptacionRequest request) {
        adaptacionService.modificar(id, request);
        return "redirect:/admin/adaptaciones";
    }

    /** Da de baja lógica la adaptación y vuelve a la lista. */
    @PostMapping("/baja")
    public String darDeBaja(@RequestParam Long id) {
        adaptacionService.darDeBaja(id);
        return "redirect:/admin/adaptaciones";
    }
}
