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

/**
 * API REST del ABML de materias (/api/materias). Consultar (GET) lo puede cualquier usuario con sesión;
 * dar de alta, modificar y dar de baja, solo el administrador.
 */
@RestController 
@RequestMapping("/api/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }
    
    /** Lista las materias activas. Responde 200. */
    @GetMapping
    public ResponseEntity<List<MateriaResponse>> obtenerMaterias() {
        return ResponseEntity.ok(materiaService.listar());
    }

    /** Busca una materia activa por id. Responde 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<MateriaResponse> obtenerMateriaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(materiaService.buscarPorId(id));
    }
    
    /** Da de alta una materia. Responde 201 con el header Location, o 400/409 si los datos no son válidos. */
    @PostMapping
    public ResponseEntity<MateriaResponse> crearMateria(@Valid @RequestBody MateriaRequest request) {
        MateriaResponse creada = materiaService.crear(request);
        return ResponseEntity.created(URI.create("/api/materias/" + creada.id())).body(creada);
    }

    /** Modifica una materia. Responde 200, 400, 404 o 409. */
    @PutMapping("/{id}")
    public ResponseEntity<MateriaResponse> editarMateria(@PathVariable Long id, @Valid @RequestBody MateriaRequest request) {
        return ResponseEntity.ok(materiaService.modificar(id, request));
    }
    
    /** Baja lógica si no tiene turnos futuros. Responde 204 sin cuerpo, o 404/409. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        materiaService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
