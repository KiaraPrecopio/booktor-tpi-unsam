package com.mdd.booktor.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdd.booktor.dtos.AdaptacionRequest;
import com.mdd.booktor.dtos.AdaptacionResponse;

import com.mdd.booktor.services.AdaptacionService;

import org.springframework.web.bind.annotation.RequestBody; // ✅ CORRECTO

import jakarta.validation.Valid;

/**
 * API REST del ABML de adaptaciones (/api/adaptaciones). Consultar (GET) lo puede cualquier usuario con sesión;
 * dar de alta, modificar y dar de baja, solo el administrador.
 */
@RestController 
@RequestMapping("/api/adaptaciones")
public class AdaptacionController {
    private final AdaptacionService adaptacionService;   

    public AdaptacionController(AdaptacionService adaptacionService) {
        this.adaptacionService = adaptacionService;
    }

    /** Lista las adaptaciones activas. Responde 200. */
    @GetMapping
    public ResponseEntity<List<AdaptacionResponse>> obtenerAdaptaciones() {
        return ResponseEntity.ok(adaptacionService.listar());
    }

    /** Busca una adaptación activa por id. Responde 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<AdaptacionResponse> obtenerAdaptacion(@PathVariable Long id) {
        return ResponseEntity.ok(adaptacionService.buscarPorId(id));
    }

    /** Da de alta una adaptación. Responde 201 con el header Location, o 400/409 si los datos no son válidos. */
    @PostMapping
    public ResponseEntity<AdaptacionResponse> crearAdaptacion(@Valid @RequestBody AdaptacionRequest request) {
        AdaptacionResponse creada = adaptacionService.crear(request);
        return ResponseEntity.created(URI.create("/api/adaptaciones/" + creada.id())).body(creada);
    }

    /** Modifica una adaptación. Responde 200, 400, 404 o 409. */
    @PutMapping("/{id}")
    public ResponseEntity<AdaptacionResponse> editarAdaptacion(@PathVariable Long id, @Valid @RequestBody AdaptacionRequest request) {
        return ResponseEntity.ok(adaptacionService.modificar(id, request));
    }
    
    /** Baja lógica si ningún tutor activo la ofrece. Responde 204 sin cuerpo, o 404/409. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        adaptacionService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
