package com.mdd.booktor.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.services.TurnoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @PostMapping("/bloques")
    public ResponseEntity<List<TurnoResponse>> generarBloque(@Valid @RequestBody BloqueTurnosRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.generarBloque(request));
    }
}
