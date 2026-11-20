package com.mdd.booktor.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.dtos.TurnoTutorResponse;
import com.mdd.booktor.services.TurnoService;

import jakarta.validation.Valid;

/** API REST de los turnos del tutor ({@code /api/turnos}). El tutor se toma del token. */
@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    /** Genera los turnos de un bloque horario. Responde 201 con los turnos creados, o 400/404/409. */
    @PostMapping("/bloques")
    public ResponseEntity<List<TurnoResponse>> generarBloque(@Valid @RequestBody BloqueTurnosRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.generarBloque(request, principal.getName()));
    }

    /** Lista los turnos futuros del tutor, con la reserva de cada turno asignado. Responde 200. */
    @GetMapping("/mis-turnos")
    public ResponseEntity<List<TurnoTutorResponse>> listarMisTurnos(Principal principal) {
        return ResponseEntity.ok(turnoService.listarProximosDeTutor(principal.getName()));
    }

    /** Da de baja un turno disponible del propio tutor. Responde 200, 403, 404 o 409. */
    @PatchMapping("/{id}/baja")
    public ResponseEntity<TurnoResponse> darDeBaja(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(turnoService.darDeBaja(id, principal.getName()));
    }
}
