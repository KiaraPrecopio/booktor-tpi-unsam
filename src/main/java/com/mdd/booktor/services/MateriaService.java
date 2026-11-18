package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.MateriaRequest;
import com.mdd.booktor.dtos.MateriaResponse;

public interface MateriaService {
    List<MateriaResponse> listar();
    MateriaResponse buscarPorId(Long id);
    MateriaResponse crear(MateriaRequest request);
    MateriaResponse modificar(Long id, MateriaRequest request);
    void darDeBaja(Long id);
}
