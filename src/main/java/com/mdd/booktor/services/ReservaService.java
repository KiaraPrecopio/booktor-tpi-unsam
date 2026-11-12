package com.mdd.booktor.services;

import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;

public interface ReservaService {
    ReservaResponse reservar(ReservaRequest request);
}
