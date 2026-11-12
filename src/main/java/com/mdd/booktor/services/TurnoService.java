package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.BajaTurnoRequest;
import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.dtos.TurnoTutorResponse;

public interface TurnoService {
    List<TurnoResponse> generarBloque(BloqueTurnosRequest request);
    List<TurnoTutorResponse> listarProximosDeTutor(Long tutorId);
    TurnoResponse darDeBaja(Long id, BajaTurnoRequest request);
}
