package com.mdd.booktor.tasks;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mdd.booktor.services.TurnoService;

/**
 * Tarea programada que cierra los turnos que ya terminaron.
 */
@Component
public class FinalizarTurnosTask {

    private final TurnoService turnoService;

    /**
     * Crea la tarea.
     *
     * @param turnoService el service que finaliza los turnos
     */
    public FinalizarTurnosTask(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    /**
     * Corre cada 5 minutos y pasa a FINALIZADO los turnos asignados cuyo horario terminó.
     */
    @Scheduled(fixedRate = 300000) // cada 5 minutos
    public void finalizarTurnosTerminados() {
        turnoService.finalizarTurnosTerminados();
    }
}
