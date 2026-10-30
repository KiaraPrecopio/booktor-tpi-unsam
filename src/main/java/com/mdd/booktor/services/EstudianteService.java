package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.EstudianteRequest;
import com.mdd.booktor.dtos.EstudianteResponse;

public interface EstudianteService {
    List<EstudianteResponse> listar();
    EstudianteResponse buscarPorId(Long id);
    EstudianteResponse crear(EstudianteRequest request);
    EstudianteResponse modificar(Long id, EstudianteRequest request);
    void darDeBaja(Long id);
}
