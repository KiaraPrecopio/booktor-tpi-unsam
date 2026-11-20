package com.mdd.booktor.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdd.booktor.dtos.EstudianteRequest;
import com.mdd.booktor.dtos.EstudianteResponse;
import com.mdd.booktor.services.EstudianteService;

import jakarta.validation.Valid;

/** API REST del ABML de estudiantes (/api/estudiantes). Solo la puede usar el administrador. */
@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    /** Lista los estudiantes activos. Responde 200. */
    @GetMapping
    public ResponseEntity<List<EstudianteResponse>> obtenerEstudiantes() {
        return ResponseEntity.ok(estudianteService.listar());
    }

    /** Busca un estudiante activo por id. Responde 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<EstudianteResponse> obtenerEstudiantePorId(@PathVariable Long id) {
        return ResponseEntity.ok(estudianteService.buscarPorId(id));
    }

    /** Da de alta un estudiante. Responde 201 con el header Location, o 400/409 si los datos no son válidos. */
    @PostMapping
    public ResponseEntity<EstudianteResponse> crearEstudiante(@Valid @RequestBody EstudianteRequest request) {
        EstudianteResponse creado = estudianteService.crear(request);
        return ResponseEntity.created(URI.create("/api/estudiantes/" + creado.id())).body(creado);
    }

    /** Modifica un estudiante. Responde 200, 400, 404 o 409. */
    @PutMapping("/{id}")
    public ResponseEntity<EstudianteResponse> editarEstudiante(@PathVariable Long id, @Valid @RequestBody EstudianteRequest request) {
        return ResponseEntity.ok(estudianteService.modificar(id, request));
    }

    /** Baja lógica si no tiene reservas activas. Responde 204 sin cuerpo, o 404/409. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        estudianteService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
