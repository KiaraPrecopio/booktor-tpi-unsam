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

/**
 * Pantallas del ABML de estudiantes para el administrador ({@code /admin/estudiantes}).
 * Usan el mismo service que la API; si el service rechaza una operación,
 * {@link WebControllerAdvice} muestra la página de error.
 */
@Controller
@RequestMapping("/admin/estudiantes")
public class EstudianteWebController {

    private final EstudianteService estudianteService;
    private final AdaptacionService adaptacionService;

    public EstudianteWebController(EstudianteService estudianteService, AdaptacionService adaptacionService) {
        this.estudianteService = estudianteService;
        this.adaptacionService = adaptacionService;
    }

    /** Muestra la lista de estudiantes y el formulario de alta. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("estudiantes", estudianteService.listar());
        model.addAttribute("adaptaciones", adaptacionService.listar());
        return "admin/estudiantes";
    }

    /** Da de alta un estudiante con los datos del formulario y vuelve a la lista. */
    @PostMapping
    public String crear(@Valid @ModelAttribute EstudianteRequest request) {
        estudianteService.crear(request);
        return "redirect:/admin/estudiantes";
    }

    /** Muestra el formulario de edición, con las adaptaciones que ya tiene marcadas. */
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

    /** Guarda los cambios y vuelve a la lista. */
    @PostMapping("/modificar")
    public String modificar(@RequestParam Long id, @Valid @ModelAttribute EstudianteRequest request) {
        estudianteService.modificar(id, request);
        return "redirect:/admin/estudiantes";
    }

    /** Da de baja lógica al estudiante y vuelve a la lista. */
    @PostMapping("/baja")
    public String darDeBaja(@RequestParam Long id) {
        estudianteService.darDeBaja(id);
        return "redirect:/admin/estudiantes";
    }
}
