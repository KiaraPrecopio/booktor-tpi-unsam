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

import com.mdd.booktor.dtos.TutorRequest;
import com.mdd.booktor.dtos.TutorResponse;
import com.mdd.booktor.services.TutorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tutores")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping
    public ResponseEntity<List<TutorResponse>> obtenerTutores() {
        return ResponseEntity.ok(tutorService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorResponse> obtenerTutorPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TutorResponse> crearTutor(@Valid @RequestBody TutorRequest request) {
        TutorResponse creado = tutorService.crear(request);
        return ResponseEntity.created(URI.create("/api/tutores/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TutorResponse> editarTutor(@PathVariable Long id, @Valid @RequestBody TutorRequest request) {
        return ResponseEntity.ok(tutorService.modificar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        tutorService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
