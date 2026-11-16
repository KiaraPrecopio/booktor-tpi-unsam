package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;

public interface ReservaService {
    ReservaResponse reservar(ReservaRequest request, String emailEstudiante);
    ReservaResponse cancelar(Long id, String emailUsuario);
    List<ReservaResponse> listarActivasFuturas();
    List<ReservaResponse> listarProximasDeEstudiante(String emailEstudiante);
}
