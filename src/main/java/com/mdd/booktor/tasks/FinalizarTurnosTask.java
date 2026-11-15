package com.mdd.booktor.tasks;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.repositories.ReservaRepository;
import com.mdd.booktor.repositories.TurnoRepository;

@Component
public class FinalizarTurnosTask {

    private final TurnoRepository turnoRepository;
    private final ReservaRepository reservaRepository;
    private final Clock clock;

    public FinalizarTurnosTask(TurnoRepository turnoRepository, ReservaRepository reservaRepository, Clock clock) {
        this.turnoRepository = turnoRepository;
        this.reservaRepository = reservaRepository;
        this.clock = clock;
    }

    @Scheduled(fixedRate = 300000) // cada 5 minutos
    @Transactional
    public void finalizarTurnosTerminados() {
        LocalDateTime ahora = LocalDateTime.now(clock);
        List<Turno> turnos = turnoRepository.buscarAsignadosTerminados(ahora);

        for (Turno turno : turnos) {
            turno.setEstado(EstadoTurno.FINALIZADO);
            reservaRepository.findByTurno_IdAndEstado(turno.getId(), EstadoReserva.ACTIVA)
                    .ifPresent(reserva -> {
                        reserva.setEstado(EstadoReserva.FINALIZADA);
                        reservaRepository.save(reserva);
                    });
        }

        turnoRepository.saveAll(turnos);
    }
}
