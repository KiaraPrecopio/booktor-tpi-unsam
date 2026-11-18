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

import com.mdd.booktor.dtos.MateriaRequest;
import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.services.MateriaService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }
    
    @GetMapping
    public ResponseEntity<List<MateriaResponse>> obtenerMaterias() {
        return ResponseEntity.ok(materiaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MateriaResponse> obtenerMateriaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(materiaService.buscarPorId(id));
    }
    
    @PostMapping
    public ResponseEntity<MateriaResponse> crearMateria(@Valid @RequestBody MateriaRequest request) {
        MateriaResponse creada = materiaService.crear(request);
        return ResponseEntity.created(URI.create("/api/materias/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MateriaResponse> editarMateria(@PathVariable Long id, @Valid @RequestBody MateriaRequest request) {
        return ResponseEntity.ok(materiaService.modificar(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        materiaService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
