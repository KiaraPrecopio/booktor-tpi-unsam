package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.TurnoResponse;

public interface TurnoService {
    List<TurnoResponse> generarBloque(BloqueTurnosRequest request);
}
