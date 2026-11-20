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

/**
 * Pantallas del ABML de materias para el administrador ({@code /admin/materias}).
 * Usan el mismo service que la API; si el service rechaza una operación,
 * {@link WebControllerAdvice} muestra la página de error.
 */
@Controller
@RequestMapping("/admin/materias")
public class MateriaWebController {

    private final MateriaService materiaService;

    public MateriaWebController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    /** Muestra la lista de materias y el formulario de alta. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("materias", materiaService.listar());
        return "admin/materias";
    }

    /** Da de alta una materia con los datos del formulario y vuelve a la lista. */
    @PostMapping
    public String crear(@Valid @ModelAttribute MateriaRequest request) {
        materiaService.crear(request);
        return "redirect:/admin/materias";
    }

    /** Muestra el formulario de edición. */
    @GetMapping("/editar")
    public String editar(@RequestParam Long id, Model model) {
        model.addAttribute("materia", materiaService.buscarPorId(id));
        return "admin/materia-editar";
    }

    /** Guarda los cambios y vuelve a la lista. */
    @PostMapping("/modificar")
    public String modificar(@RequestParam Long id, @Valid @ModelAttribute MateriaRequest request) {
        materiaService.modificar(id, request);
        return "redirect:/admin/materias";
    }

    /** Da de baja lógica la materia y vuelve a la lista. */
    @PostMapping("/baja")
    public String darDeBaja(@RequestParam Long id) {
        materiaService.darDeBaja(id);
        return "redirect:/admin/materias";
    }
}
