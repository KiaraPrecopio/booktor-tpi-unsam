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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mdd.booktor.dtos.EstudianteRequest;
import com.mdd.booktor.dtos.EstudianteResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.DatoInvalidoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.Adaptacion;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.AdaptacionRepository;
import com.mdd.booktor.repositories.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class EstudianteServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AdaptacionRepository adaptacionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EstudianteServiceImpl service;

    private Usuario estudianteExistente() {
        return new Usuario("Juan", "Pérez", "juan@mail.com", "hash-anterior", Rol.ESTUDIANTE, true);
    }

    @Test
    void crearEstudianteValido() {
        Set<Long> ids = Set.of(1L);
        when(usuarioRepository.existsByEmailIgnoreCase("juan@mail.com")).thenReturn(false);
        when(adaptacionRepository.findByIdInAndActivoTrue(ids))
                .thenReturn(List.of(new Adaptacion("Material visual", null, true)));
        when(passwordEncoder.encode("secreta123")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        EstudianteResponse respuesta = service.crear(
                new EstudianteRequest(" Juan ", "Pérez", "Juan@Mail.com", "secreta123", ids));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals(Rol.ESTUDIANTE, guardado.getRol());
        assertTrue(guardado.isActivo());
        assertEquals("hash-bcrypt", guardado.getPassword());
        assertEquals("juan@mail.com", guardado.getEmail());
        assertEquals("Juan", guardado.getNombre());
        assertEquals(1, guardado.getAdaptaciones().size());

        assertEquals("juan@mail.com", respuesta.email());
        assertEquals("Material visual", respuesta.adaptaciones().get(0).nombre());
    }

    @Test
    void crearSinPasswordLanza400() {
        assertThrows(DatoInvalidoException.class,
                () -> service.crear(new EstudianteRequest("Juan", "Pérez", "juan@mail.com", null, null)));

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void crearConEmailRepetidoLanza409() {
        when(usuarioRepository.existsByEmailIgnoreCase("juan@mail.com")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.crear(new EstudianteRequest("Juan", "Pérez", "juan@mail.com", "secreta123", null)));

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void crearConAdaptacionInexistenteLanza404() {
        Set<Long> ids = Set.of(1L, 99L);
        when(usuarioRepository.existsByEmailIgnoreCase("juan@mail.com")).thenReturn(false);
        when(adaptacionRepository.findByIdInAndActivoTrue(ids))
                .thenReturn(List.of(new Adaptacion("Material visual", null, true)));

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.crear(new EstudianteRequest("Juan", "Pérez", "juan@mail.com", "secreta123", ids)));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearSinAdaptacionesNoLasBusca() {
        when(usuarioRepository.existsByEmailIgnoreCase("juan@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("secreta123")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        EstudianteResponse respuesta = service.crear(
                new EstudianteRequest("Juan", "Pérez", "juan@mail.com", "secreta123", null));

        assertTrue(respuesta.adaptaciones().isEmpty());
        verify(adaptacionRepository, never()).findByIdInAndActivoTrue(any());
    }

    @Test
    void buscarPorIdInexistenteLanza404() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(99L, Rol.ESTUDIANTE)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void modificarSinPasswordConservaLaAnterior() {
        Usuario existente = estudianteExistente();
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.ESTUDIANTE)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("juan@mail.com", 1L)).thenReturn(false);
        when(usuarioRepository.save(existente)).thenReturn(existente);

        service.modificar(1L, new EstudianteRequest("Juan Carlos", "Pérez", "juan@mail.com", null, null));

        assertEquals("hash-anterior", existente.getPassword());
        assertEquals("Juan Carlos", existente.getNombre());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void modificarConPasswordLaEncripta() {
        Usuario existente = estudianteExistente();
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.ESTUDIANTE)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("juan@mail.com", 1L)).thenReturn(false);
        when(passwordEncoder.encode("nueva12345")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(existente)).thenReturn(existente);

        service.modificar(1L, new EstudianteRequest("Juan", "Pérez", "juan@mail.com", "nueva12345", null));

        assertEquals("hash-nuevo", existente.getPassword());
    }

    @Test
    void modificarManteniendoSuEmailFunciona() {
        Usuario existente = estudianteExistente();
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.ESTUDIANTE)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("juan@mail.com", 1L)).thenReturn(false);
        when(usuarioRepository.save(existente)).thenReturn(existente);

        EstudianteResponse respuesta = service.modificar(1L,
                new EstudianteRequest("Juan", "Pérez", "juan@mail.com", null, null));

        assertEquals("juan@mail.com", respuesta.email());
    }

    @Test
    void modificarConEmailDeOtroLanza409() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.ESTUDIANTE))
                .thenReturn(Optional.of(estudianteExistente()));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("ana@mail.com", 1L)).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.modificar(1L, new EstudianteRequest("Juan", "Pérez", "ana@mail.com", null, null)));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void darDeBajaPoneActivoEnFalse() {
        Usuario existente = estudianteExistente();
        when(usuarioRepository.findByIdAndRolAndActivoTrue(1L, Rol.ESTUDIANTE)).thenReturn(Optional.of(existente));

        service.darDeBaja(1L);

        assertFalse(existente.isActivo());
        verify(usuarioRepository).save(existente);
    }

    @Test
    void darDeBajaInexistenteLanza404() {
        when(usuarioRepository.findByIdAndRolAndActivoTrue(99L, Rol.ESTUDIANTE)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.darDeBaja(99L));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void listarPideSoloEstudiantes() {
        when(usuarioRepository.findByRolAndActivoTrue(Rol.ESTUDIANTE)).thenReturn(List.of(
                estudianteExistente(),
                new Usuario("Ana", "Gómez", "ana@mail.com", "hash", Rol.ESTUDIANTE, true)));

        List<EstudianteResponse> respuesta = service.listar();

        assertEquals(2, respuesta.size());
        assertEquals("Ana", respuesta.get(1).nombre());
        verify(usuarioRepository).findByRolAndActivoTrue(Rol.ESTUDIANTE);
    }
}
