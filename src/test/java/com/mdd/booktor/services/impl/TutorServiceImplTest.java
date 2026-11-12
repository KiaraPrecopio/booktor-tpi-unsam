package com.mdd.booktor.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mdd.booktor.dtos.TutorRequest;
import com.mdd.booktor.dtos.TutorResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.DatoInvalidoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.Adaptacion;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Materia;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Reserva;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.AdaptacionRepository;
import com.mdd.booktor.repositories.ReservaRepository;
import com.mdd.booktor.repositories.TurnoRepository;
import com.mdd.booktor.repositories.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TutorServiceImplTest {

    private static final Set<Long> IDS = Set.of(1L);

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AdaptacionRepository adaptacionRepository;

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private TutorServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        service = new TutorServiceImpl(usuarioRepository, adaptacionRepository, passwordEncoder, turnoRepository, reservaRepository, clock);
    }

    private Usuario tutorExistente() {
        Usuario tutor = new Usuario("Ana", "Gómez", "ana@mail.com", "hash-anterior", Rol.TUTOR, true);
        tutor.setDuracionTurnoMinutos(60);
        return tutor;
    }

    private TutorRequest request(String email, String password, Integer duracion, Set<Long> ids) {
        return new TutorRequest("Ana", "Gómez", email, password, duracion, ids);
    }

    private void adaptacionesActivas() {
        when(adaptacionRepository.findByIdInAndActivoTrue(IDS))
                .thenReturn(List.of(new Adaptacion("Material visual", null, true)));
    }

    @Test
    void crearTutorValido() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(false);
        adaptacionesActivas();
        when(passwordEncoder.encode("secreta123")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        TutorResponse respuesta = service.crear(
                new TutorRequest(" Ana ", " Gómez ", "Ana@Mail.com", "secreta123", 45, IDS));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals(Rol.TUTOR, guardado.getRol());
        assertTrue(guardado.isActivo());
        assertEquals("hash-bcrypt", guardado.getPassword());
        assertEquals("ana@mail.com", guardado.getEmail());
        assertEquals("Ana", guardado.getNombre());
        assertEquals("Gómez", guardado.getApellido());
        assertEquals(45, guardado.getDuracionTurnoMinutos());
        assertEquals(1, guardado.getAdaptaciones().size());

        assertEquals(45, respuesta.duracionTurnoMinutos());
        assertEquals("Material visual", respuesta.adaptaciones().get(0).nombre());
    }

    @Test
    void crearSinPasswordLanza400() {
        assertThrows(DatoInvalidoException.class,
                () -> service.crear(request("ana@mail.com", null, 60, IDS)));

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void crearConEmailRepetidoLanza409() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.crear(request("ana@mail.com", "secreta123", 60, IDS)));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearConAdaptacionInactivaOInexistenteLanza404ConLosIds() {
        Set<Long> ids = Set.of(1L, 99L);
        when(usuarioRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(false);
        when(adaptacionRepository.findByIdInAndActivoTrue(ids))
                .thenReturn(List.of(new Adaptacion("Material visual", null, true)));

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> service.crear(request("ana@mail.com", "secreta123", 60, ids)));

        assertTrue(ex.getMessage().contains("99"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void buscarPorIdInexistenteLanza404() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(99L, Rol.TUTOR)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void modificarCambiaDuracionYConservaPasswordSiNoSeEnvia() {
        Usuario existente = tutorExistente();
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("ana@mail.com", 1L)).thenReturn(false);
        adaptacionesActivas();
        when(usuarioRepository.save(existente)).thenReturn(existente);

        TutorResponse respuesta = service.modificar(1L, request("ana@mail.com", null, 30, IDS));

        assertEquals(30, existente.getDuracionTurnoMinutos());
        assertEquals("hash-anterior", existente.getPassword());
        assertEquals(30, respuesta.duracionTurnoMinutos());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void modificarConPasswordLaEncripta() {
        Usuario existente = tutorExistente();
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("ana@mail.com", 1L)).thenReturn(false);
        adaptacionesActivas();
        when(passwordEncoder.encode("nueva12345")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(existente)).thenReturn(existente);

        service.modificar(1L, request("ana@mail.com", "nueva12345", 60, IDS));

        assertEquals("hash-nuevo", existente.getPassword());
    }

    @Test
    void modificarConEmailDeOtroLanza409() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.of(tutorExistente()));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("otro@mail.com", 1L)).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.modificar(1L, request("otro@mail.com", null, 60, IDS)));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void darDeBajaPoneActivoEnFalse() {
        Usuario existente = tutorExistente();
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.of(existente));

        service.darDeBaja(1L);

        assertFalse(existente.isActivo());
        verify(usuarioRepository).save(existente);
    }

    @Test
    void modificarDuracionConTurnosAsignadosLanza409() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.of(tutorExistente()));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("ana@mail.com", 1L)).thenReturn(false);
        when(turnoRepository.existsByTutor_IdAndEstadoAndInicioAfter(1L, EstadoTurno.ASIGNADO, AHORA)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.modificar(1L, request("ana@mail.com", null, 30, IDS)));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void modificarSinCambiarLaDuracionNoRevisaLosTurnos() {
        Usuario existente = tutorExistente();
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("ana@mail.com", 1L)).thenReturn(false);
        adaptacionesActivas();
        when(usuarioRepository.save(existente)).thenReturn(existente);

        service.modificar(1L, request("ana@mail.com", null, 60, IDS));

        verify(turnoRepository, never()).existsByTutor_IdAndEstadoAndInicioAfter(any(), any(), any());
    }

    @Test
    void darDeBajaConReservasActivasLanza409() {
        Usuario existente = tutorExistente();
        Usuario estudiante = new Usuario("Juan", "Pérez", "juan@mail.com", "hash", Rol.ESTUDIANTE, true);
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 11, 14, 0);
        Turno turno = new Turno(existente, new Materia("Matemática", null, true), inicio, inicio.plusHours(1), Modalidad.VIRTUAL, "https://meet.com/x", null);
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.TUTOR)).thenReturn(Optional.of(existente));
        when(reservaRepository.buscarActivasFuturasDeTutor(1L, AHORA))
                .thenReturn(List.of(new Reserva(turno, estudiante, AHORA, null)));

        ConflictoException ex = assertThrows(ConflictoException.class, () -> service.darDeBaja(1L));

        assertTrue(ex.getMessage().startsWith("No se puede dar de baja al tutor"));
        assertTrue(existente.isActivo());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void darDeBajaInexistenteLanza404() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(99L, Rol.TUTOR)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.darDeBaja(99L));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void listarPideSoloTutores() {
        when(usuarioRepository.findByRolAndActivoTrue(Rol.TUTOR)).thenReturn(List.of(tutorExistente()));

        List<TutorResponse> respuesta = service.listar();

        assertEquals(1, respuesta.size());
        assertEquals(60, respuesta.get(0).duracionTurnoMinutos());
        verify(usuarioRepository).findByRolAndActivoTrue(Rol.TUTOR);
    }
}
