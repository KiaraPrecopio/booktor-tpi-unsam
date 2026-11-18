package com.mdd.booktor.services.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.dtos.TurnoTutorResponse;
import com.mdd.booktor.exceptions.AccesoDenegadoException;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.DatoInvalidoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Materia;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Reserva;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.MateriaRepository;
import com.mdd.booktor.repositories.ReservaRepository;
import com.mdd.booktor.repositories.TurnoRepository;
import com.mdd.booktor.repositories.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TurnoServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);
    private static final LocalDate MANANA = LocalDate.of(2026, 11, 11);
    private static final String LINK = "https://meet.google.com/abc";
    private static final String EMAIL_TUTOR = "ana@mail.com";

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private TurnoServiceImpl service;
    private Usuario tutor;
    private Materia materia;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        service = new TurnoServiceImpl(turnoRepository, usuarioRepository, materiaRepository, reservaRepository, clock);
        tutor = new Usuario("Ana", "Gómez", "ana@mail.com", "hash", Rol.TUTOR, true);
        tutor.setDuracionTurnoMinutos(60);
        ReflectionTestUtils.setField(tutor, "id", 1L);
        materia = new Materia("Matemática", null, true);
    }

    private BloqueTurnosRequest bloque(LocalDate fecha, int desde, int hasta) {
        return new BloqueTurnosRequest(2L, fecha, LocalTime.of(desde, 0), LocalTime.of(hasta, 0), Modalidad.VIRTUAL, LINK, null);
    }

    private void tutorYMateriaActivos() {
        when(usuarioRepository.findByEmailIgnoreCaseAndRolAndActivoTrue(EMAIL_TUTOR, Rol.TUTOR)).thenReturn(Optional.of(tutor));
        when(materiaRepository.findByIdAndActivoTrue(2L)).thenReturn(Optional.of(materia));
    }

    private void guardarDevuelveLoMismo() {
        when(turnoRepository.saveAll(anyList())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void bloqueDeCuatroHorasGeneraCuatroTurnosDisponibles() {
        tutorYMateriaActivos();
        guardarDevuelveLoMismo();

        List<TurnoResponse> turnos = service.generarBloque(bloque(MANANA, 14, 18), EMAIL_TUTOR);

        assertEquals(4, turnos.size());
        assertEquals(MANANA.atTime(14, 0), turnos.get(0).inicio());
        assertEquals(MANANA.atTime(15, 0), turnos.get(0).fin());
        assertEquals(MANANA.atTime(17, 0), turnos.get(3).inicio());
        assertEquals(MANANA.atTime(18, 0), turnos.get(3).fin());
        turnos.forEach(t -> {
            assertEquals(EstadoTurno.DISPONIBLE, t.estado());
            assertEquals("Matemática", t.materia());
            assertEquals(Modalidad.VIRTUAL, t.modalidad());
            assertEquals(LINK, t.link());
            assertNull(t.lugar());
        });
    }

    @Test
    void bloqueDeUnaDuracionGeneraUnSoloTurno() {
        tutorYMateriaActivos();
        guardarDevuelveLoMismo();

        List<TurnoResponse> turnos = service.generarBloque(bloque(MANANA, 14, 15), EMAIL_TUTOR);

        assertEquals(1, turnos.size());
    }

    @Test
    void bloquePresencialGuardaElLugarYNoElLink() {
        tutorYMateriaActivos();
        guardarDevuelveLoMismo();

        List<TurnoResponse> turnos = service.generarBloque(new BloqueTurnosRequest(2L, MANANA,
                LocalTime.of(14, 0), LocalTime.of(15, 0), Modalidad.PRESENCIAL, LINK, " Aula 3 "), EMAIL_TUTOR);

        assertEquals("Aula 3", turnos.get(0).lugar());
        assertNull(turnos.get(0).link());
    }

    @Test
    void tutorInexistenteLanza404() {
        when(usuarioRepository.findByEmailIgnoreCaseAndRolAndActivoTrue(EMAIL_TUTOR, Rol.TUTOR)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.generarBloque(bloque(MANANA, 14, 18), EMAIL_TUTOR));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void materiaInactivaLanza404() {
        when(usuarioRepository.findByEmailIgnoreCaseAndRolAndActivoTrue(EMAIL_TUTOR, Rol.TUTOR)).thenReturn(Optional.of(tutor));
        when(materiaRepository.findByIdAndActivoTrue(2L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.generarBloque(bloque(MANANA, 14, 18), EMAIL_TUTOR));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void horaHastaIgualOAnteriorLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(bloque(MANANA, 14, 14), EMAIL_TUTOR));
        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(bloque(MANANA, 18, 14), EMAIL_TUTOR));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void bloqueEnElPasadoLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(bloque(AHORA.toLocalDate(), 9, 11), EMAIL_TUTOR));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void bloqueQueEmpiezaAhoraSePermite() {
        tutorYMateriaActivos();
        guardarDevuelveLoMismo();

        List<TurnoResponse> turnos = service.generarBloque(bloque(AHORA.toLocalDate(), 10, 11), EMAIL_TUTOR);

        assertEquals(1, turnos.size());
    }

    @Test
    void bloqueQueNoEsMultiploDeLaDuracionLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(new BloqueTurnosRequest(2L, MANANA,
                LocalTime.of(14, 0), LocalTime.of(15, 30), Modalidad.VIRTUAL, LINK, null), EMAIL_TUTOR));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void virtualSinLinkLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(new BloqueTurnosRequest(2L, MANANA,
                LocalTime.of(14, 0), LocalTime.of(15, 0), Modalidad.VIRTUAL, " ", null), EMAIL_TUTOR));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void presencialSinLugarLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(new BloqueTurnosRequest(2L, MANANA,
                LocalTime.of(14, 0), LocalTime.of(15, 0), Modalidad.PRESENCIAL, null, null), EMAIL_TUTOR));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void bloqueSuperpuestoLanza409ConLosTurnosConLosQueChoca() {
        tutorYMateriaActivos();
        when(turnoRepository.buscarSuperpuestos(1L, MANANA.atTime(14, 0), MANANA.atTime(18, 0))).thenReturn(List.of(
                new Turno(tutor, materia, MANANA.atTime(17, 0), MANANA.atTime(18, 0), Modalidad.VIRTUAL, LINK, null)));

        ConflictoException ex = assertThrows(ConflictoException.class, () -> service.generarBloque(bloque(MANANA, 14, 18), EMAIL_TUTOR));

        assertEquals("El bloque se superpone con estos turnos del tutor: 11/11/2026 17:00-18:00.", ex.getMessage());
        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void buscaSuperpuestosConElInicioYFinDelBloque() {
        tutorYMateriaActivos();
        guardarDevuelveLoMismo();

        service.generarBloque(bloque(MANANA, 14, 16), EMAIL_TUTOR);

        verify(turnoRepository).buscarSuperpuestos(1L, MANANA.atTime(14, 0), MANANA.atTime(16, 0));
        verify(turnoRepository).saveAll(any());
    }

    private TurnoResponse bajaComoTutorDeLaSesion(Long turnoId) {
        when(usuarioRepository.findByEmailIgnoreCaseAndRolAndActivoTrue(EMAIL_TUTOR, Rol.TUTOR)).thenReturn(Optional.of(tutor));
        return service.darDeBaja(turnoId, EMAIL_TUTOR);
    }

    private Turno turnoDelTutor(LocalDateTime inicio) {
        ReflectionTestUtils.setField(tutor, "id", 1L);
        return new Turno(tutor, materia, inicio, inicio.plusHours(1), Modalidad.VIRTUAL, LINK, null);
    }

    @Test
    void listarProximosDelTutorMuestraElEstudianteSoloEnLosAsignados() {
        Turno libre = turnoDelTutor(MANANA.atTime(14, 0));
        Turno asignado = turnoDelTutor(MANANA.atTime(15, 0));
        asignado.setEstado(EstadoTurno.ASIGNADO);
        Usuario juan = new Usuario("Juan", "Pérez", "juan@mail.com", "hash", Rol.ESTUDIANTE, true);
        when(usuarioRepository.findByEmailIgnoreCaseAndRolAndActivoTrue(EMAIL_TUTOR, Rol.TUTOR)).thenReturn(Optional.of(tutor));
        when(turnoRepository.buscarFuturosDeTutor(1L, List.of(EstadoTurno.DISPONIBLE, EstadoTurno.ASIGNADO), AHORA))
                .thenReturn(List.of(libre, asignado));
        when(reservaRepository.buscarActivasFuturasDeTutor(1L, AHORA))
                .thenReturn(List.of(new Reserva(asignado, juan, AHORA, "Repasar límites")));

        List<TurnoTutorResponse> respuesta = service.listarProximosDeTutor(EMAIL_TUTOR);

        assertEquals(2, respuesta.size());
        assertEquals(EstadoTurno.DISPONIBLE, respuesta.get(0).estado());
        assertNull(respuesta.get(0).estudiante());
        assertEquals(EstadoTurno.ASIGNADO, respuesta.get(1).estado());
        assertEquals("Juan Pérez", respuesta.get(1).estudiante());
        assertEquals("Repasar límites", respuesta.get(1).objetivo());
    }

    @Test
    void listarProximosDeTutorInexistenteLanza404() {
        when(usuarioRepository.findByEmailIgnoreCaseAndRolAndActivoTrue("nadie@mail.com", Rol.TUTOR)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.listarProximosDeTutor("nadie@mail.com"));
    }

    @Test
    void darDeBajaUnTurnoDisponibleLoPasaADadoDeBaja() {
        Turno turno = turnoDelTutor(MANANA.atTime(14, 0));
        when(turnoRepository.findById(5L)).thenReturn(Optional.of(turno));

        TurnoResponse respuesta = bajaComoTutorDeLaSesion(5L);

        assertEquals(EstadoTurno.DADO_DE_BAJA, turno.getEstado());
        assertEquals(EstadoTurno.DADO_DE_BAJA, respuesta.estado());
        verify(turnoRepository).save(turno);
    }

    @Test
    void darDeBajaUnTurnoAsignadoLanza409ConLaReservaACancelar() {
        Turno turno = turnoDelTutor(MANANA.atTime(14, 0));
        turno.setEstado(EstadoTurno.ASIGNADO);
        Reserva reserva = new Reserva(turno, new Usuario("Juan", "Pérez", "juan@mail.com", "hash", Rol.ESTUDIANTE, true), AHORA, null);
        ReflectionTestUtils.setField(reserva, "id", 8L);
        when(turnoRepository.findById(5L)).thenReturn(Optional.of(turno));
        when(reservaRepository.findByTurno_IdAndEstado(5L, EstadoReserva.ACTIVA)).thenReturn(Optional.of(reserva));

        ConflictoException ex = assertThrows(ConflictoException.class, () -> bajaComoTutorDeLaSesion(5L));

        assertEquals("No se puede dar de baja el turno, primero hay que cancelar la reserva [8] de Juan Pérez.", ex.getMessage());
        assertEquals(EstadoTurno.ASIGNADO, turno.getEstado());
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void darDeBajaUnTurnoDeOtroTutorLanza403() {
        when(turnoRepository.findById(5L)).thenReturn(Optional.of(turnoDelTutor(MANANA.atTime(14, 0))));

        Usuario otroTutor = new Usuario("Luis", "Díaz", "luis@mail.com", "hash", Rol.TUTOR, true);
        ReflectionTestUtils.setField(otroTutor, "id", 2L);
        when(usuarioRepository.findByEmailIgnoreCaseAndRolAndActivoTrue("luis@mail.com", Rol.TUTOR))
                .thenReturn(Optional.of(otroTutor));

        assertThrows(AccesoDenegadoException.class, () -> service.darDeBaja(5L, "luis@mail.com"));

        verify(turnoRepository, never()).save(any());
    }

    @Test
    void darDeBajaUnTurnoYaDadoDeBajaLanza409() {
        Turno turno = turnoDelTutor(MANANA.atTime(14, 0));
        turno.setEstado(EstadoTurno.DADO_DE_BAJA);
        when(turnoRepository.findById(5L)).thenReturn(Optional.of(turno));

        assertThrows(ConflictoException.class, () -> bajaComoTutorDeLaSesion(5L));
    }

    @Test
    void darDeBajaTurnoInexistenteLanza404() {
        when(turnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> bajaComoTutorDeLaSesion(99L));
    }

    private Turno turnoAsignado(Long id, LocalDateTime inicio, LocalDateTime fin) {
        Turno turno = new Turno(tutor, materia, inicio, fin, Modalidad.VIRTUAL, LINK, null);
        turno.setEstado(EstadoTurno.ASIGNADO);
        ReflectionTestUtils.setField(turno, "id", id);
        return turno;
    }

    private Reserva reservaActiva(Turno turno) {
        Usuario estudiante = new Usuario("Juan", "Pérez", "juan@mail.com", "hash", Rol.ESTUDIANTE, true);
        return new Reserva(turno, estudiante, AHORA.minusDays(1), "Repasar derivadas");
    }

    @Test
    void turnoAsignadoTerminadoHaceUnMinutoPasaAFinalizadoYSuReservaAFinalizada() {
        Turno turno = turnoAsignado(1L, AHORA.minusMinutes(61), AHORA.minusMinutes(1));
        Reserva reserva = reservaActiva(turno);
        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of(turno));
        when(reservaRepository.findByTurno_IdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(Optional.of(reserva));

        service.finalizarTurnosTerminados();

        assertEquals(EstadoTurno.FINALIZADO, turno.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        verify(turnoRepository).saveAll(List.of(turno));
        verify(reservaRepository).save(reserva);
    }

    @Test
    void turnoQueTerminaExactamenteAhoraPasaAFinalizado() {
        Turno turno = turnoAsignado(2L, AHORA.minusMinutes(60), AHORA);
        Reserva reserva = reservaActiva(turno);
        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of(turno));
        when(reservaRepository.findByTurno_IdAndEstado(2L, EstadoReserva.ACTIVA)).thenReturn(Optional.of(reserva));

        service.finalizarTurnosTerminados();

        assertEquals(EstadoTurno.FINALIZADO, turno.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
    }

    @Test
    void variosTurnosTerminadosSeFinalizanTodos() {
        Turno turno1 = turnoAsignado(1L, AHORA.minusMinutes(120), AHORA.minusMinutes(60));
        Turno turno2 = turnoAsignado(2L, AHORA.minusMinutes(60), AHORA);
        Reserva reserva1 = reservaActiva(turno1);
        Reserva reserva2 = reservaActiva(turno2);
        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of(turno1, turno2));
        when(reservaRepository.findByTurno_IdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(Optional.of(reserva1));
        when(reservaRepository.findByTurno_IdAndEstado(2L, EstadoReserva.ACTIVA)).thenReturn(Optional.of(reserva2));

        service.finalizarTurnosTerminados();

        assertEquals(EstadoTurno.FINALIZADO, turno1.getEstado());
        assertEquals(EstadoTurno.FINALIZADO, turno2.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, reserva1.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, reserva2.getEstado());
    }

    // Solo se busca la reserva ACTIVA del turno, así que las CANCELADAS nunca llegan acá.
    @Test
    void turnoSinReservaActivaSeFinalizaSinModificarReservas() {
        Turno turno = turnoAsignado(3L, AHORA.minusMinutes(61), AHORA.minusMinutes(1));
        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of(turno));
        when(reservaRepository.findByTurno_IdAndEstado(3L, EstadoReserva.ACTIVA)).thenReturn(Optional.empty());

        service.finalizarTurnosTerminados();

        assertEquals(EstadoTurno.FINALIZADO, turno.getEstado());
        verify(reservaRepository, never()).save(any());
    }

    // Qué turnos se finalizan (solo ASIGNADOS con fin <= ahora) lo decide la consulta
    // buscarAsignadosTerminados; con el repositorio mockeado no se puede probar acá.
    // Este test comprueba que, si la consulta no trae turnos, no se modifica nada.
    @Test
    void sinTurnosTerminadosNoModificaNada() {
        when(turnoRepository.buscarAsignadosTerminados(AHORA)).thenReturn(List.of());

        service.finalizarTurnosTerminados();

        verify(reservaRepository, never()).save(any());
        verify(turnoRepository).saveAll(List.of());
    }
}
