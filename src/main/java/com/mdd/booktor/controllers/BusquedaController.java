package com.mdd.booktor.controllers;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.dtos.TutorDisponibleResponse;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.services.BusquedaService;

@RestController
@RequestMapping("/api/busqueda")
public class BusquedaController {

    private final BusquedaService busquedaService;

    public BusquedaController(BusquedaService busquedaService) {
        this.busquedaService = busquedaService;
    }

    @GetMapping("/materias")
    public ResponseEntity<List<MateriaResponse>> buscarMaterias(@RequestParam(required = false) String nombre) {
        return ResponseEntity.ok(busquedaService.buscarMaterias(nombre));
    }

    @GetMapping("/materias/{id}/tutores")
    public ResponseEntity<List<TutorDisponibleResponse>> buscarTutores(@PathVariable Long id,
            @RequestParam(required = false) Set<Long> adaptaciones,
            @RequestParam(required = false) Modalidad modalidad) {
        return ResponseEntity.ok(busquedaService.buscarTutores(id, adaptaciones, modalidad));
    }
}
