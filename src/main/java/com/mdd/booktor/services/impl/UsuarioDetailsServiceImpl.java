package com.mdd.booktor.services.impl;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.UsuarioRepository;

/** Le dice a Spring Security cómo buscar un usuario al iniciar sesión. */
@Service
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Busca el usuario por email y devuelve su hash BCrypt y su rol. Si está dado de baja,
     * queda deshabilitado y no puede iniciar sesión.
     *
     * @param email el email con el que intenta entrar
     * @return los datos que usa Spring Security para comparar la contraseña
     * @throws UsernameNotFoundException si no existe un usuario con ese email
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con ese email."));

        return User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .disabled(!usuario.isActivo())
                .build();
    }
}
