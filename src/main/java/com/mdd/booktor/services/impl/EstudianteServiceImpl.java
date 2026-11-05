package com.mdd.booktor.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.mdd.booktor.services.EstudianteService;
import com.mdd.booktor.utils.EstudianteMapper;

@Service
public class EstudianteServiceImpl implements EstudianteService {

    private final UsuarioRepository usuarioRepository;
    private final AdaptacionRepository adaptacionRepository;
    private final PasswordEncoder passwordEncoder;

    public EstudianteServiceImpl(UsuarioRepository usuarioRepository, AdaptacionRepository adaptacionRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.adaptacionRepository = adaptacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteResponse> listar() {
        return usuarioRepository.findByRolAndActivoTrue(Rol.ESTUDIANTE).stream()
                .map(EstudianteMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteResponse buscarPorId(Long id) {
        return EstudianteMapper.toResponse(buscarEstudianteActivo(id));
    }

    @Override
    @Transactional
    public EstudianteResponse crear(EstudianteRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new DatoInvalidoException("La contraseña es obligatoria para dar de alta un estudiante.");
        }

        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictoException("Ya existe un usuario con el email [" + email + "], vuelva a intentar con otro email.");
        }

        Set<Adaptacion> adaptaciones = buscarAdaptacionesActivas(request.adaptacionIds());

        Usuario estudiante = new Usuario(
            request.nombre().trim(),
            request.apellido().trim(),
            email,
            passwordEncoder.encode(request.password()),
            Rol.ESTUDIANTE,
            true
        );
        estudiante.setAdaptaciones(adaptaciones);

        Usuario guardado = usuarioRepository.save(estudiante);
        return EstudianteMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public EstudianteResponse modificar(Long id, EstudianteRequest request) {
        Usuario estudiante = buscarEstudianteActivo(id);

        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictoException("Ya existe un usuario con el email [" + email + "], vuelva a intentar con otro email.");
        }

        Set<Adaptacion> adaptaciones = buscarAdaptacionesActivas(request.adaptacionIds());

        estudiante.setNombre(request.nombre().trim());
        estudiante.setApellido(request.apellido().trim());
        estudiante.setEmail(email);
        if (request.password() != null && !request.password().isBlank()) {
            estudiante.setPassword(passwordEncoder.encode(request.password()));
        }
        estudiante.setAdaptaciones(adaptaciones);

        Usuario actualizado = usuarioRepository.save(estudiante);
        return EstudianteMapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public void darDeBaja(Long id) {
        Usuario estudiante = buscarEstudianteActivo(id);
        estudiante.setActivo(false);
        usuarioRepository.save(estudiante);
    }

    private Usuario buscarEstudianteActivo(Long id) {
        return usuarioRepository.findByIdAndRolAndActivoTrue(id, Rol.ESTUDIANTE)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un estudiante activo con el id [" + id + "]."));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Set<Adaptacion> buscarAdaptacionesActivas(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<Adaptacion> encontradas = adaptacionRepository.findByIdInAndActivoTrue(ids);
        if (encontradas.size() != ids.size()) {
            Set<Long> idsEncontrados = encontradas.stream()
                    .map(Adaptacion::getId)
                    .collect(Collectors.toSet());
            List<Long> faltantes = ids.stream()
                    .filter(id -> !idsEncontrados.contains(id))
                    .sorted()
                    .toList();
            throw new RecursoNoEncontradoException("No existen adaptaciones activas con los ids " + faltantes + ".");
        }
        return new HashSet<>(encontradas);
    }
}
