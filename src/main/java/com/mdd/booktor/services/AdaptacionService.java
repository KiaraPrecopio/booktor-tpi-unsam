package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.AdaptacionRequest;
import com.mdd.booktor.dtos.AdaptacionResponse;

public interface AdaptacionService {
    List<AdaptacionResponse> listar();
    AdaptacionResponse buscarPorId(Long id);
    AdaptacionResponse crear(AdaptacionRequest request);
    AdaptacionResponse modificar(Long id, AdaptacionRequest request);
    void darDeBaja(Long id);
}
