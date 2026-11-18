package com.mdd.booktor.services;

import java.util.List;
import java.util.Set;

import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.dtos.TutorDisponibleResponse;
import com.mdd.booktor.models.Modalidad;

public interface BusquedaService {
    List<MateriaResponse> buscarMaterias(String nombre);
    List<TutorDisponibleResponse> buscarTutores(Long materiaId, Set<Long> adaptacionIds, Modalidad modalidad);
}
