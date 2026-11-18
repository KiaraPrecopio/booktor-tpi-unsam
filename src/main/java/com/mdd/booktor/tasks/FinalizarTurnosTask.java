package com.mdd.booktor.tasks;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mdd.booktor.services.TurnoService;

@Component
public class FinalizarTurnosTask {

    private final TurnoService turnoService;

    public FinalizarTurnosTask(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @Scheduled(fixedRate = 300000) // cada 5 minutos
    public void finalizarTurnosTerminados() {
        turnoService.finalizarTurnosTerminados();
    }
}
