package com.mdd.booktor.controllers;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.services.ReservaService;

import jakarta.validation.Valid;

/**
 * API REST de reservas ({@code /api/reservas}). El usuario se toma del token ({@link java.security.Principal}),
 * nunca de un dato que mande el cliente.
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /** El estudiante reserva un turno. Responde 201, o 404/409 si no se puede reservar. */
    @PostMapping
    public ResponseEntity<ReservaResponse> reservar(@Valid @RequestBody ReservaRequest request, Principal principal) {
        ReservaResponse creada = reservaService.reservar(request, principal.getName());
        return ResponseEntity.created(URI.create("/api/reservas/" + creada.id())).body(creada);
    }

    /**
     * Cancela una reserva: la puede cancelar el estudiante que reservó, el tutor del turno o un administrador.
     * Responde 200, 403, 404 o 409.
     */
    @PatchMapping("/{id}/cancelacion")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(reservaService.cancelar(id, principal.getName()));
    }

    /** El administrador lista todas las reservas activas de turnos que no empezaron. Responde 200. */
    @GetMapping("/activas")
    public ResponseEntity<List<ReservaResponse>> listarActivasFuturas() {
        return ResponseEntity.ok(reservaService.listarActivasFuturas());
    }

    /** El estudiante lista sus reservas activas y futuras. Responde 200. */
    @GetMapping("/mis-reservas")
    public ResponseEntity<List<ReservaResponse>> listarMisReservas(Principal principal) {
        return ResponseEntity.ok(reservaService.listarProximasDeEstudiante(principal.getName()));
    }
}
