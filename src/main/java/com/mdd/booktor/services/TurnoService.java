package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.dtos.TurnoTutorResponse;

public interface TurnoService {
    List<TurnoResponse> generarBloque(BloqueTurnosRequest request, String emailTutor);
    List<TurnoTutorResponse> listarProximosDeTutor(String emailTutor);
    TurnoResponse darDeBaja(Long id, String emailTutor);
    void finalizarTurnosTerminados();
}
