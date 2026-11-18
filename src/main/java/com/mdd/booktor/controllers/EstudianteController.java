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

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public ResponseEntity<List<EstudianteResponse>> obtenerEstudiantes() {
        return ResponseEntity.ok(estudianteService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstudianteResponse> obtenerEstudiantePorId(@PathVariable Long id) {
        return ResponseEntity.ok(estudianteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EstudianteResponse> crearEstudiante(@Valid @RequestBody EstudianteRequest request) {
        EstudianteResponse creado = estudianteService.crear(request);
        return ResponseEntity.created(URI.create("/api/estudiantes/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstudianteResponse> editarEstudiante(@PathVariable Long id, @Valid @RequestBody EstudianteRequest request) {
        return ResponseEntity.ok(estudianteService.modificar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        estudianteService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
