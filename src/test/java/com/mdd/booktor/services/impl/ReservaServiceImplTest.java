package com.mdd.booktor.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
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
import com.mdd.booktor.repositories.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private ReservaServiceImpl service;
    private Usuario estudiante;
    private Usuario tutor;
    private Materia materia;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        service = new ReservaServiceImpl(reservaRepository, turnoRepository, usuarioRepository, clock);
        estudiante = new Usuario("Juan", "Pérez", "juan@mail.com", "hash", Rol.ESTUDIANTE, true);
        tutor = new Usuario("Ana", "Gómez", "ana@mail.com", "hash", Rol.TUTOR, true);
        materia = new Materia("Matemática", null, true);
    }

    private Turno turnoQueEmpieza(LocalDateTime inicio) {
        return new Turno(tutor, materia, inicio, inicio.plusMinutes(30), Modalidad.VIRTUAL, "https://meet.com/x", null);
    }

    private void estudianteYTurno(Turno turno) {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.ESTUDIANTE)).thenReturn(Optional.of(estudiante));
        when(turnoRepository.findById(2L)).thenReturn(Optional.of(turno));
    }

    private void guardarDevuelveLoMismo() {
        when(reservaRepository.saveAndFlush(any(Reserva.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void reservaValidaCreaReservaActivaYAsignaElTurno() {
        Turno turno = turnoQueEmpieza(AHORA.plusDays(1));
        estudianteYTurno(turno);
        guardarDevuelveLoMismo();

        ReservaResponse respuesta = service.reservar(new ReservaRequest(1L, 2L, "  Repasar derivadas "));

        ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).saveAndFlush(captor.capture());
        Reserva guardada = captor.getValue();
        assertEquals(EstadoReserva.ACTIVA, guardada.getEstado());
        assertEquals(AHORA, guardada.getFechaReserva());
        assertEquals("Repasar derivadas", guardada.getObjetivo());
        assertEquals(EstadoTurno.ASIGNADO, turno.getEstado());
        verify(turnoRepository).save(turno);

        assertEquals("Matemática", respuesta.materia());
        assertEquals("Ana Gómez", respuesta.tutor());
        assertEquals("https://meet.com/x", respuesta.link());
    }

    @Test
    void objetivoVacioSeGuardaComoNulo() {
        estudianteYTurno(turnoQueEmpieza(AHORA.plusDays(1)));
        guardarDevuelveLoMismo();

        ReservaResponse respuesta = service.reservar(new ReservaRequest(1L, 2L, "   "));

        assertNull(respuesta.objetivo());
    }

    @Test
    void estudianteInexistenteOInactivoLanza404() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.ESTUDIANTE)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.reservar(new ReservaRequest(1L, 2L, null)));

        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    void turnoInexistenteLanza404() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.ESTUDIANTE)).thenReturn(Optional.of(estudiante));
        when(turnoRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.reservar(new ReservaRequest(1L, 2L, null)));
    }

    @Test
    void turnoYaAsignadoLanza409() {
        Turno turno = turnoQueEmpieza(AHORA.plusDays(1));
        turno.setEstado(EstadoTurno.ASIGNADO);
        estudianteYTurno(turno);

        assertThrows(ConflictoException.class, () -> service.reservar(new ReservaRequest(1L, 2L, null)));

        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    void tutorInactivoLanza409() {
        tutor.setActivo(false);
        estudianteYTurno(turnoQueEmpieza(AHORA.plusDays(1)));

        assertThrows(ConflictoException.class, () -> service.reservar(new ReservaRequest(1L, 2L, null)));

        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    void reservarExactamenteCincoMinutosAntesSePermite() {
        estudianteYTurno(turnoQueEmpieza(AHORA.plusMinutes(5)));
        guardarDevuelveLoMismo();

        ReservaResponse respuesta = service.reservar(new ReservaRequest(1L, 2L, null));

        assertEquals(EstadoReserva.ACTIVA, respuesta.estado());
    }

    @Test
    void reservarCuatroMinutosAntesLanza409() {
        estudianteYTurno(turnoQueEmpieza(AHORA.plusMinutes(4)));

        assertThrows(ConflictoException.class, () -> service.reservar(new ReservaRequest(1L, 2L, null)));

        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    void reservarUnTurnoQueYaEmpezoLanza409() {
        estudianteYTurno(turnoQueEmpieza(AHORA.minusMinutes(10)));

        assertThrows(ConflictoException.class, () -> service.reservar(new ReservaRequest(1L, 2L, null)));
    }

    @Test
    void reservaSuperpuestaLanza409ConElTurnoConElQueChoca() {
        LocalDateTime inicio = AHORA.plusDays(1).withHour(14);
        Turno turno = turnoQueEmpieza(inicio);
        estudianteYTurno(turno);
        Turno otro = new Turno(tutor, new Materia("Física", null, true), inicio.minusMinutes(15), inicio.plusMinutes(15),
                Modalidad.PRESENCIAL, null, "Aula 3");
        when(reservaRepository.buscarActivasSuperpuestas(1L, turno.getInicio(), turno.getFin()))
                .thenReturn(List.of(new Reserva(otro, estudiante, AHORA, null)));

        ConflictoException ex = assertThrows(ConflictoException.class, () -> service.reservar(new ReservaRequest(1L, 2L, null)));

        assertEquals("El estudiante ya tiene una reserva que se superpone con este turno: Física 11/11/2026 13:45-14:15.", ex.getMessage());
        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    void buscaSuperpuestasConElHorarioDelTurno() {
        Turno turno = turnoQueEmpieza(AHORA.plusDays(1).withHour(14).withMinute(30));
        estudianteYTurno(turno);
        guardarDevuelveLoMismo();

        service.reservar(new ReservaRequest(1L, 2L, null));

        verify(reservaRepository).buscarActivasSuperpuestas(1L, turno.getInicio(), turno.getFin());
    }

    @Test
    void siLaBaseRechazaLaReservaPorqueOtroLaTomoLanza409() {
        estudianteYTurno(turnoQueEmpieza(AHORA.plusDays(1)));
        when(reservaRepository.saveAndFlush(any(Reserva.class))).thenThrow(new DataIntegrityViolationException("ux_reserva_turno_activa"));

        ConflictoException ex = assertThrows(ConflictoException.class, () -> service.reservar(new ReservaRequest(1L, 2L, null)));

        assertEquals("Este turno acaba de ser reservado por otra persona.", ex.getMessage());
        verify(turnoRepository, never()).save(any());
    }
}
