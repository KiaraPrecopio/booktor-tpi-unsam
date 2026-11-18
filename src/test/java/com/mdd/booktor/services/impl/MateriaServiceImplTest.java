package com.mdd.booktor.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.mdd.booktor.dtos.MateriaRequest;
import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Materia;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.MateriaRepository;
import com.mdd.booktor.repositories.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class MateriaServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);

    @Mock
    private MateriaRepository repository;

    @Mock
    private TurnoRepository turnoRepository;

    private MateriaServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        service = new MateriaServiceImpl(repository, turnoRepository, clock);
    }

    @Test
    void crearMateriaValida() {
        when(repository.existsByNombreIgnoreCaseAndActivoTrue("Matemática")).thenReturn(false);
        Materia guardada = new Materia("Matemática", "Álgebra y análisis", true);
        when(repository.save(any(Materia.class))).thenReturn(guardada);

        MateriaResponse respuesta = service.crear(new MateriaRequest("  Matemática  ", "Álgebra y análisis"));

        // Captura la materia que el service le pasó a save, para revisar cómo la armó
        ArgumentCaptor<Materia> captor = ArgumentCaptor.forClass(Materia.class);
        verify(repository).save(captor.capture());
        assertEquals("Matemática", captor.getValue().getNombre());
        assertTrue(captor.getValue().getActivo());

        assertEquals("Matemática", respuesta.nombre());
        assertEquals("Álgebra y análisis", respuesta.descripcion());
    }

    @Test
    void crearConNombreRepetidoLanza409() {
        when(repository.existsByNombreIgnoreCaseAndActivoTrue("Matemática")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.crear(new MateriaRequest("Matemática", null)));

        verify(repository, never()).save(any());
    }

    @Test
    void buscarPorIdDevuelveLaMateria() {
        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(new Materia("Matemática", "Álgebra", true)));

        MateriaResponse respuesta = service.buscarPorId(1L);

        assertEquals("Matemática", respuesta.nombre());
    }

    @Test
    void buscarPorIdInexistenteLanza404() {
        when(repository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void modificarInexistenteLanza404() {
        when(repository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.modificar(99L, new MateriaRequest("Física", null)));

        verify(repository, never()).save(any());
    }

    @Test
    void modificarConNombreDeOtraLanza409() {
        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(new Materia("Matemática", "Álgebra", true)));
        when(repository.existsByNombreIgnoreCaseAndActivoTrueAndIdNot("Física", 1L)).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.modificar(1L, new MateriaRequest("Física", null)));

        verify(repository, never()).save(any());
    }

    @Test
    void modificarManteniendoSuNombreFunciona() {
        // Caso límite: la propia materia no cuenta como nombre repetido
        Materia existente = new Materia("Matemática", "Álgebra", true);
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByNombreIgnoreCaseAndActivoTrueAndIdNot("Matemática", 1L)).thenReturn(false);
        when(repository.save(existente)).thenReturn(existente);

        MateriaResponse respuesta = service.modificar(1L, new MateriaRequest("Matemática", "Álgebra y análisis"));

        assertEquals("Matemática", respuesta.nombre());
        assertEquals("Álgebra y análisis", respuesta.descripcion());
    }

    @Test
    void darDeBajaPoneActivoEnFalse() {
        Materia materia = new Materia("Matemática", "Álgebra", true);
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(materia));

        service.darDeBaja(1L);

        // Baja lógica: se guarda con activo en false, no se borra
        assertFalse(materia.getActivo());
        verify(repository).save(materia);
    }

    @Test
    void darDeBajaConTurnosFuturosLanza409() {
        Materia materia = new Materia("Matemática", "Álgebra", true);
        Usuario ana = new Usuario("Ana", "Gómez", "ana@mail.com", "hash", Rol.TUTOR, true);
        LocalDateTime manana = AHORA.plusDays(1);
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(materia));
        when(turnoRepository.buscarFuturosDeMateria(1L, List.of(EstadoTurno.DISPONIBLE, EstadoTurno.ASIGNADO), AHORA))
                .thenReturn(List.of(
                        new Turno(ana, materia, manana, manana.plusHours(1), Modalidad.VIRTUAL, "https://meet.com/x", null),
                        new Turno(ana, materia, manana.plusHours(1), manana.plusHours(2), Modalidad.VIRTUAL, "https://meet.com/x", null)));

        ConflictoException ex = assertThrows(ConflictoException.class, () -> service.darDeBaja(1L));

        assertEquals("No se puede dar de baja la materia, tiene 2 turnos futuros de los tutores: Ana Gómez.", ex.getMessage());
        assertTrue(materia.getActivo());
        verify(repository, never()).save(any());
    }

    @Test
    void darDeBajaInexistenteLanza404() {
        when(repository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.darDeBaja(99L));

        verify(repository, never()).save(any());
    }

    @Test
    void listarDevuelveLasActivas() {
        when(repository.findByActivoTrue()).thenReturn(List.of(
                new Materia("Matemática", "Álgebra", true),
                new Materia("Física", "Mecánica", true)));

        List<MateriaResponse> respuesta = service.listar();

        assertEquals(2, respuesta.size());
        assertEquals("Física", respuesta.get(1).nombre());
    }
}
