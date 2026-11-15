package com.mdd.booktor.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsServiceImpl service;

    @Test
    void usuarioActivoSeCargaConSuHashYSuRol() {
        Usuario tutor = new Usuario("Ana", "Gómez", "ana@mail.com", "$2a$12$hash", Rol.TUTOR, true);
        when(usuarioRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.of(tutor));

        UserDetails usuario = service.loadUserByUsername("ana@mail.com");

        assertEquals("ana@mail.com", usuario.getUsername());
        assertEquals("$2a$12$hash", usuario.getPassword());
        assertEquals("ROLE_TUTOR", usuario.getAuthorities().iterator().next().getAuthority());
        assertTrue(usuario.isEnabled());
    }

    @Test
    void usuarioDadoDeBajaQuedaDeshabilitado() {
        Usuario estudiante = new Usuario("Juan", "Pérez", "juan@mail.com", "$2a$12$hash", Rol.ESTUDIANTE, false);
        when(usuarioRepository.findByEmailIgnoreCase("juan@mail.com")).thenReturn(Optional.of(estudiante));

        UserDetails usuario = service.loadUserByUsername("juan@mail.com");

        assertFalse(usuario.isEnabled());
    }

    @Test
    void emailInexistenteLanzaUsernameNotFoundException() {
        when(usuarioRepository.findByEmailIgnoreCase("nadie@mail.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nadie@mail.com"));
    }
}
