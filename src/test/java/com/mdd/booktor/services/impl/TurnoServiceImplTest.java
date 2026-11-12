package com.mdd.booktor.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.DatoInvalidoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Materia;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.MateriaRepository;
import com.mdd.booktor.repositories.TurnoRepository;
import com.mdd.booktor.repositories.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TurnoServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);
    private static final LocalDate MANANA = LocalDate.of(2026, 11, 11);
    private static final String LINK = "https://meet.google.com/abc";

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MateriaRepository materiaRepository;

    private TurnoServiceImpl service;
    private Usuario tutor;
    private Materia materia;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        service = new TurnoServiceImpl(turnoRepository, usuarioRepository, materiaRepository, clock);
        tutor = new Usuario("Ana", "Gómez", "ana@mail.com", "hash", Rol.TUTOR, true);
        tutor.setDuracionTurnoMinutos(60);
        materia = new Materia("Matemática", null, true);
    }

    private BloqueTurnosRequest bloque(LocalDate fecha, int desde, int hasta) {
        return new BloqueTurnosRequest(1L, 2L, fecha, LocalTime.of(desde, 0), LocalTime.of(hasta, 0), Modalidad.VIRTUAL, LINK, null);
    }

    private void tutorYMateriaActivos() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.of(tutor));
        when(materiaRepository.findByIdAndActivoTrue(2L)).thenReturn(Optional.of(materia));
    }

    private void guardarDevuelveLoMismo() {
        when(turnoRepository.saveAll(anyList())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void bloqueDeCuatroHorasGeneraCuatroTurnosDisponibles() {
        tutorYMateriaActivos();
        guardarDevuelveLoMismo();

        List<TurnoResponse> turnos = service.generarBloque(bloque(MANANA, 14, 18));

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

        List<TurnoResponse> turnos = service.generarBloque(bloque(MANANA, 14, 15));

        assertEquals(1, turnos.size());
    }

    @Test
    void bloquePresencialGuardaElLugarYNoElLink() {
        tutorYMateriaActivos();
        guardarDevuelveLoMismo();

        List<TurnoResponse> turnos = service.generarBloque(new BloqueTurnosRequest(1L, 2L, MANANA,
                LocalTime.of(14, 0), LocalTime.of(15, 0), Modalidad.PRESENCIAL, LINK, " Aula 3 "));

        assertEquals("Aula 3", turnos.get(0).lugar());
        assertNull(turnos.get(0).link());
    }

    @Test
    void tutorInexistenteLanza404() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.generarBloque(bloque(MANANA, 14, 18)));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void materiaInactivaLanza404() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.of(tutor));
        when(materiaRepository.findByIdAndActivoTrue(2L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.generarBloque(bloque(MANANA, 14, 18)));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void horaHastaIgualOAnteriorLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(bloque(MANANA, 14, 14)));
        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(bloque(MANANA, 18, 14)));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void bloqueEnElPasadoLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(bloque(AHORA.toLocalDate(), 9, 11)));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void bloqueQueEmpiezaAhoraSePermite() {
        tutorYMateriaActivos();
        guardarDevuelveLoMismo();

        List<TurnoResponse> turnos = service.generarBloque(bloque(AHORA.toLocalDate(), 10, 11));

        assertEquals(1, turnos.size());
    }

    @Test
    void bloqueQueNoEsMultiploDeLaDuracionLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(new BloqueTurnosRequest(1L, 2L, MANANA,
                LocalTime.of(14, 0), LocalTime.of(15, 30), Modalidad.VIRTUAL, LINK, null)));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void virtualSinLinkLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(new BloqueTurnosRequest(1L, 2L, MANANA,
                LocalTime.of(14, 0), LocalTime.of(15, 0), Modalidad.VIRTUAL, " ", null)));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void presencialSinLugarLanza400() {
        tutorYMateriaActivos();

        assertThrows(DatoInvalidoException.class, () -> service.generarBloque(new BloqueTurnosRequest(1L, 2L, MANANA,
                LocalTime.of(14, 0), LocalTime.of(15, 0), Modalidad.PRESENCIAL, null, null)));

        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void bloqueSuperpuestoLanza409ConLosTurnosConLosQueChoca() {
        tutorYMateriaActivos();
        when(turnoRepository.buscarSuperpuestos(1L, MANANA.atTime(14, 0), MANANA.atTime(18, 0))).thenReturn(List.of(
                new Turno(tutor, materia, MANANA.atTime(17, 0), MANANA.atTime(18, 0), Modalidad.VIRTUAL, LINK, null)));

        ConflictoException ex = assertThrows(ConflictoException.class, () -> service.generarBloque(bloque(MANANA, 14, 18)));

        assertEquals("El bloque se superpone con estos turnos del tutor: 11/11/2026 17:00-18:00.", ex.getMessage());
        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    void buscaSuperpuestosConElInicioYFinDelBloque() {
        tutorYMateriaActivos();
        guardarDevuelveLoMismo();

        service.generarBloque(bloque(MANANA, 14, 16));

        verify(turnoRepository).buscarSuperpuestos(1L, MANANA.atTime(14, 0), MANANA.atTime(16, 0));
        verify(turnoRepository).saveAll(any());
    }
}
