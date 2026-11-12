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
import org.springframework.test.util.ReflectionTestUtils;

import com.mdd.booktor.dtos.CancelacionRequest;
import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.exceptions.AccesoDenegadoException;
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

    private Usuario conId(Usuario usuario, Long id) {
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private Reserva reservaActiva(Turno turno) {
        turno.setEstado(EstadoTurno.ASIGNADO);
        Reserva reserva = new Reserva(turno, conId(estudiante, 1L), AHORA.minusDays(1), null);
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        return reserva;
    }

    private void quienCancela(Usuario usuario) {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
    }

    @Test
    void elEstudianteCancelaSuReservaYElTurnoVuelveADisponible() {
        Turno turno = turnoQueEmpieza(AHORA.plusDays(1));
        Reserva reserva = reservaActiva(turno);
        quienCancela(estudiante);

        ReservaResponse respuesta = service.cancelar(10L, new CancelacionRequest(1L));

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(Rol.ESTUDIANTE, reserva.getCanceladaPor());
        assertEquals(AHORA, reserva.getFechaCancelacion());
        assertEquals(EstadoTurno.DISPONIBLE, turno.getEstado());
        assertEquals(Rol.ESTUDIANTE, respuesta.canceladaPor());
        verify(reservaRepository).save(reserva);
        verify(turnoRepository).save(turno);
    }

    @Test
    void elTutorDelTurnoPuedeCancelar() {
        conId(tutor, 3L);
        Reserva reserva = reservaActiva(turnoQueEmpieza(AHORA.plusDays(1)));
        quienCancela(tutor);

        service.cancelar(10L, new CancelacionRequest(3L));

        assertEquals(Rol.TUTOR, reserva.getCanceladaPor());
    }

    @Test
    void unAdministradorPuedeCancelar() {
        Reserva reserva = reservaActiva(turnoQueEmpieza(AHORA.plusDays(1)));
        quienCancela(conId(new Usuario("Admin", "Booktor", "admin@mail.com", "hash", Rol.ADMINISTRADOR, true), 9L));

        service.cancelar(10L, new CancelacionRequest(9L));

        assertEquals(Rol.ADMINISTRADOR, reserva.getCanceladaPor());
    }

    @Test
    void otroEstudianteNoPuedeCancelarLanza403() {
        reservaActiva(turnoQueEmpieza(AHORA.plusDays(1)));
        quienCancela(conId(new Usuario("María", "López", "maria@mail.com", "hash", Rol.ESTUDIANTE, true), 4L));

        assertThrows(AccesoDenegadoException.class, () -> service.cancelar(10L, new CancelacionRequest(4L)));

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void otroTutorNoPuedeCancelarLanza403() {
        conId(tutor, 3L);
        reservaActiva(turnoQueEmpieza(AHORA.plusDays(1)));
        quienCancela(conId(new Usuario("Luis", "Díaz", "luis@mail.com", "hash", Rol.TUTOR, true), 5L));

        assertThrows(AccesoDenegadoException.class, () -> service.cancelar(10L, new CancelacionRequest(5L)));
    }

    @Test
    void cancelarUnaReservaYaCanceladaLanza409() {
        Reserva reserva = reservaActiva(turnoQueEmpieza(AHORA.plusDays(1)));
        reserva.setEstado(EstadoReserva.CANCELADA);
        quienCancela(estudiante);

        assertThrows(ConflictoException.class, () -> service.cancelar(10L, new CancelacionRequest(1L)));

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void cancelarUnTurnoQueYaEmpezoLanza409() {
        reservaActiva(turnoQueEmpieza(AHORA));
        quienCancela(estudiante);

        assertThrows(ConflictoException.class, () -> service.cancelar(10L, new CancelacionRequest(1L)));

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void cancelarReservaInexistenteLanza404() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.cancelar(99L, new CancelacionRequest(1L)));
    }

    @Test
    void despuesDeCancelarOtroEstudiantePuedeReservarElMismoTurno() {
        Turno turno = turnoQueEmpieza(AHORA.plusDays(1));
        reservaActiva(turno);
        quienCancela(estudiante);
        service.cancelar(10L, new CancelacionRequest(1L));

        Usuario maria = new Usuario("María", "López", "maria@mail.com", "hash", Rol.ESTUDIANTE, true);
        when(usuarioRepository.findByIdAndRolAndActivoTrue(4L, Rol.ESTUDIANTE)).thenReturn(Optional.of(maria));
        when(turnoRepository.findById(2L)).thenReturn(Optional.of(turno));
        guardarDevuelveLoMismo();

        ReservaResponse nueva = service.reservar(new ReservaRequest(4L, 2L, null));

        assertEquals(EstadoReserva.ACTIVA, nueva.estado());
        assertEquals(EstadoTurno.ASIGNADO, turno.getEstado());
    }

    @Test
    void listarActivasFuturasUsaLaHoraActual() {
        when(reservaRepository.buscarActivasFuturas(AHORA))
                .thenReturn(List.of(new Reserva(turnoQueEmpieza(AHORA.plusDays(1)), estudiante, AHORA, null)));

        List<ReservaResponse> respuesta = service.listarActivasFuturas();

        assertEquals(1, respuesta.size());
        assertEquals("Juan Pérez", respuesta.get(0).estudiante());
    }
}
