package com.mdd.booktor.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.dtos.TutorDisponibleResponse;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.Adaptacion;
import com.mdd.booktor.models.Materia;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.MateriaRepository;
import com.mdd.booktor.repositories.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class BusquedaServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);
    private static final LocalDateTime MANANA = AHORA.plusDays(1).withHour(14);

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private TurnoRepository turnoRepository;

    private BusquedaServiceImpl service;
    private Materia materia;
    private Adaptacion visual;
    private Adaptacion auditivo;
    private Usuario ana;
    private Usuario luis;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        service = new BusquedaServiceImpl(materiaRepository, turnoRepository, clock);
        materia = new Materia("Matemática", null, true);
        visual = adaptacion(1L, "Material visual");
        auditivo = adaptacion(2L, "Apoyo auditivo");
        ana = tutor("Ana", visual, auditivo);
        luis = tutor("Luis", visual);
    }

    private Adaptacion adaptacion(Long id, String nombre) {
        Adaptacion adaptacion = new Adaptacion(nombre, null, true);
        ReflectionTestUtils.setField(adaptacion, "id", id);
        return adaptacion;
    }

    private Usuario tutor(String nombre, Adaptacion... adaptaciones) {
        Usuario tutor = new Usuario(nombre, "Pérez", nombre.toLowerCase() + "@mail.com", "hash", Rol.TUTOR, true);
        tutor.setAdaptaciones(Set.of(adaptaciones));
        return tutor;
    }

    private Turno turno(Usuario tutor, LocalDateTime inicio, Modalidad modalidad) {
        return new Turno(tutor, materia, inicio, inicio.plusHours(1), modalidad,
                modalidad == Modalidad.VIRTUAL ? "https://meet.com/x" : null,
                modalidad == Modalidad.PRESENCIAL ? "Aula 3" : null);
    }

    private void turnosDisponibles(Turno... turnos) {
        when(materiaRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(materia));
        when(turnoRepository.buscarDisponiblesDeMateria(1L, AHORA)).thenReturn(List.of(turnos));
    }

    @Test
    void buscarMateriasUsaElNombreSinEspaciosYLaHoraActual() {
        when(materiaRepository.buscarConTurnosDisponibles("mate", AHORA)).thenReturn(List.of(materia));

        List<MateriaResponse> respuesta = service.buscarMaterias("  mate ");

        assertEquals(1, respuesta.size());
        assertEquals("Matemática", respuesta.get(0).nombre());
    }

    @Test
    void buscarMateriasSinNombreLasTraeTodas() {
        service.buscarMaterias(null);

        verify(materiaRepository).buscarConTurnosDisponibles("", AHORA);
    }

    @Test
    void sinFiltrosAgrupaLosTurnosPorTutor() {
        turnosDisponibles(
                turno(ana, MANANA, Modalidad.VIRTUAL),
                turno(luis, MANANA, Modalidad.PRESENCIAL),
                turno(ana, MANANA.plusHours(1), Modalidad.PRESENCIAL));

        List<TutorDisponibleResponse> respuesta = service.buscarTutores(1L, null, null);

        assertEquals(2, respuesta.size());
        assertEquals("Ana", respuesta.get(0).nombre());
        assertEquals(2, respuesta.get(0).turnos().size());
        assertEquals(List.of("Apoyo auditivo", "Material visual"),
                respuesta.get(0).adaptaciones().stream().map(a -> a.nombre()).toList());
        assertEquals("Luis", respuesta.get(1).nombre());
        assertEquals(1, respuesta.get(1).turnos().size());
    }

    @Test
    void conVariasAdaptacionesSoloApareceElTutorQueOfreceTodas() {
        turnosDisponibles(
                turno(ana, MANANA, Modalidad.VIRTUAL),
                turno(luis, MANANA, Modalidad.VIRTUAL));

        List<TutorDisponibleResponse> respuesta = service.buscarTutores(1L, Set.of(1L, 2L), null);

        assertEquals(1, respuesta.size());
        assertEquals("Ana", respuesta.get(0).nombre());
    }

    @Test
    void conUnaAdaptacionAparecenTodosLosQueLaOfrecen() {
        turnosDisponibles(
                turno(ana, MANANA, Modalidad.VIRTUAL),
                turno(luis, MANANA, Modalidad.VIRTUAL));

        List<TutorDisponibleResponse> respuesta = service.buscarTutores(1L, Set.of(1L), null);

        assertEquals(2, respuesta.size());
    }

    @Test
    void filtroPorModalidadSoloDejaEsosTurnos() {
        turnosDisponibles(
                turno(ana, MANANA, Modalidad.VIRTUAL),
                turno(ana, MANANA.plusHours(1), Modalidad.PRESENCIAL),
                turno(luis, MANANA, Modalidad.VIRTUAL));

        List<TutorDisponibleResponse> respuesta = service.buscarTutores(1L, null, Modalidad.PRESENCIAL);

        assertEquals(1, respuesta.size());
        assertEquals("Ana", respuesta.get(0).nombre());
        assertEquals(1, respuesta.get(0).turnos().size());
        assertEquals(Modalidad.PRESENCIAL, respuesta.get(0).turnos().get(0).modalidad());
    }

    @Test
    void filtroDeModalidadSeCombinaConElDeAdaptaciones() {
        turnosDisponibles(
                turno(ana, MANANA, Modalidad.VIRTUAL),
                turno(luis, MANANA.plusHours(1), Modalidad.PRESENCIAL));

        List<TutorDisponibleResponse> respuesta = service.buscarTutores(1L, Set.of(1L, 2L), Modalidad.PRESENCIAL);

        assertTrue(respuesta.isEmpty());
    }

    @Test
    void sinTurnosDisponiblesDevuelveListaVacia() {
        turnosDisponibles();

        assertTrue(service.buscarTutores(1L, null, null).isEmpty());
    }

    @Test
    void materiaInexistenteLanza404() {
        when(materiaRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarTutores(99L, null, null));

        verify(turnoRepository, never()).buscarDisponiblesDeMateria(any(), any());
    }
}
