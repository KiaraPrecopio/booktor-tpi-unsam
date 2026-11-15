package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.TutorRequest;
import com.mdd.booktor.dtos.TutorResponse;

public interface TutorService {
    List<TutorResponse> listar();
    TutorResponse buscarPorId(Long id);
    TutorResponse crear(TutorRequest request);
    TutorResponse modificar(Long id, TutorRequest request);
    void darDeBaja(Long id);
}
