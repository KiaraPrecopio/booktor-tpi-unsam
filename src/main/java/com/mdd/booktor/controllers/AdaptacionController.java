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

@RestController 
@RequestMapping("/api/adaptaciones")
public class AdaptacionController {
    private final AdaptacionService adaptacionService;   

    public AdaptacionController(AdaptacionService adaptacionService) {
        this.adaptacionService = adaptacionService;
    }

    @GetMapping
    public ResponseEntity<List<AdaptacionResponse>> obtenerAdaptaciones() {
        return ResponseEntity.ok(adaptacionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdaptacionResponse> obtenerAdaptacion(@PathVariable Long id) {
        return ResponseEntity.ok(adaptacionService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AdaptacionResponse> crearAdaptacion(@Valid @RequestBody AdaptacionRequest request) {
        AdaptacionResponse creada = adaptacionService.crear(request);
        return ResponseEntity.created(URI.create("/api/adaptaciones/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdaptacionResponse> editarAdaptacion(@PathVariable Long id, @Valid @RequestBody AdaptacionRequest request) {
        return ResponseEntity.ok(adaptacionService.modificar(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        adaptacionService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
