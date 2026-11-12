package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.CancelacionRequest;
import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;

public interface ReservaService {
    ReservaResponse reservar(ReservaRequest request);
    ReservaResponse cancelar(Long id, CancelacionRequest request);
    List<ReservaResponse> listarActivasFuturas();
    List<ReservaResponse> listarProximasDeEstudiante(Long estudianteId);
}
