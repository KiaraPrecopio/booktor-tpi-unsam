package com.mdd.booktor.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Materia;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Reserva;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.ReservaRepository;
import com.mdd.booktor.repositories.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class FinalizarTurnosTaskTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private FinalizarTurnosTask task;
    private Usuario tutor;
    private Usuario estudiante;
    private Materia materia;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        task = new FinalizarTurnosTask(turnoRepository, reservaRepository, clock);
        tutor = new Usuario("Ana", "Gómez", "ana@mail.com", "hash", Rol.TUTOR, true);
        estudiante = new Usuario("Juan", "Pérez", "juan@mail.com", "hash", Rol.ESTUDIANTE, true);
        materia = new Materia("Matemática", "Álgebra", true);
    }

    private Turno crearTurno(Long id, LocalDateTime inicio, LocalDateTime fin, EstadoTurno estado) {
        Turno turno = new Turno(tutor, materia, inicio, fin, Modalidad.VIRTUAL, "https://meet.com/x", null);
        turno.setEstado(estado);
        ReflectionTestUtils.setField(turno, "id", id);
        return turno;
    }

    private Reserva crearReserva(Long id, Turno turno, EstadoReserva estado) {
        Reserva reserva = new Reserva(turno, estudiante, AHORA.minusDays(1), "Repasar derivadas");
        reserva.setEstado(estado);
        ReflectionTestUtils.setField(reserva, "id", id);
        return reserva;
    }

    @Test
    void turnoAsignadoTerminadoHaceUnMinutoPasaAFinalizadoYSuReservaAFinalizada() {
        // Turno que terminó hace un minuto: inicio 09:29, fin 09:59 (ahora es 10:00)
        Turno turno = crearTurno(1L, AHORA.minusMinutes(31), AHORA.minusMinutes(1), EstadoTurno.ASIGNADO);
        Reserva reserva = crearReserva(10L, turno, EstadoReserva.ACTIVA);

        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of(turno));
        when(reservaRepository.findByTurno_IdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(Optional.of(reserva));

        task.finalizarTurnosTerminados();

        assertEquals(EstadoTurno.FINALIZADO, turno.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());

        verify(turnoRepository).saveAll(List.of(turno));
        verify(reservaRepository).save(reserva);
    }

    @Test
    void turnoQueTerminaEnUnMinutoNoSeFinaliza() {
        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of());

        task.finalizarTurnosTerminados();

        verify(reservaRepository, never()).save(any());
        verify(turnoRepository).saveAll(List.of());
    }

    @Test
    void turnoQueTerminaExactamenteAhoraPasaAFinalizado() {
        Turno turno = crearTurno(2L, AHORA.minusMinutes(30), AHORA, EstadoTurno.ASIGNADO);
        Reserva reserva = crearReserva(20L, turno, EstadoReserva.ACTIVA);

        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of(turno));
        when(reservaRepository.findByTurno_IdAndEstado(2L, EstadoReserva.ACTIVA)).thenReturn(Optional.of(reserva));

        task.finalizarTurnosTerminados();

        assertEquals(EstadoTurno.FINALIZADO, turno.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());

        verify(turnoRepository).saveAll(List.of(turno));
        verify(reservaRepository).save(reserva);
    }

    @Test
    void turnosDisponiblesODadosDeBajaNoSonModificados() {
        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of());

        task.finalizarTurnosTerminados();

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void reservaCanceladaNoSeModifica() {
        Turno turno = crearTurno(3L, AHORA.minusMinutes(31), AHORA.minusMinutes(1), EstadoTurno.ASIGNADO);
        Reserva reservaCancelada = crearReserva(30L, turno, EstadoReserva.CANCELADA);

        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of(turno));
        when(reservaRepository.findByTurno_IdAndEstado(3L, EstadoReserva.ACTIVA)).thenReturn(Optional.empty());

        task.finalizarTurnosTerminados();

        assertEquals(EstadoTurno.FINALIZADO, turno.getEstado());
        assertEquals(EstadoReserva.CANCELADA, reservaCancelada.getEstado());

        verify(reservaRepository, never()).save(any());
        verify(turnoRepository).saveAll(List.of(turno));
    }

    @Test
    void cuandoNoHayTurnosTerminadosNoModificaNada() {
        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of());

        task.finalizarTurnosTerminados();

        verify(reservaRepository, never()).save(any());
        verify(turnoRepository).saveAll(List.of());
    }

    @Test
    void multiplesTurnosTerminadosSeFinalizanTodos() {
        Turno turno1 = crearTurno(1L, AHORA.minusMinutes(60), AHORA.minusMinutes(30), EstadoTurno.ASIGNADO);
        Reserva reserva1 = crearReserva(11L, turno1, EstadoReserva.ACTIVA);

        Turno turno2 = crearTurno(2L, AHORA.minusMinutes(30), AHORA.minusMinutes(5), EstadoTurno.ASIGNADO);
        Reserva reserva2 = crearReserva(12L, turno2, EstadoReserva.ACTIVA);

        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of(turno1, turno2));
        when(reservaRepository.findByTurno_IdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(Optional.of(reserva1));
        when(reservaRepository.findByTurno_IdAndEstado(2L, EstadoReserva.ACTIVA)).thenReturn(Optional.of(reserva2));

        task.finalizarTurnosTerminados();

        assertEquals(EstadoTurno.FINALIZADO, turno1.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, reserva1.getEstado());
        assertEquals(EstadoTurno.FINALIZADO, turno2.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, reserva2.getEstado());

        verify(turnoRepository).saveAll(List.of(turno1, turno2));
        verify(reservaRepository).save(reserva1);
        verify(reservaRepository).save(reserva2);
    }
}
