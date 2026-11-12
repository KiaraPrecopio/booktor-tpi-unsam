package com.mdd.booktor.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdd.booktor.dtos.CancelacionRequest;
import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.services.ReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> reservar(@Valid @RequestBody ReservaRequest request) {
        ReservaResponse creada = reservaService.reservar(request);
        return ResponseEntity.created(URI.create("/api/reservas/" + creada.id())).body(creada);
    }

    @PatchMapping("/{id}/cancelacion")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable Long id, @Valid @RequestBody CancelacionRequest request) {
        return ResponseEntity.ok(reservaService.cancelar(id, request));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<ReservaResponse>> listarActivasFuturas() {
        return ResponseEntity.ok(reservaService.listarActivasFuturas());
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<ReservaResponse>> listarProximasDeEstudiante(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(reservaService.listarProximasDeEstudiante(estudianteId));
    }
}
