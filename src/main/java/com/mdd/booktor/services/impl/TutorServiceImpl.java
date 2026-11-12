package com.mdd.booktor.services.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mdd.booktor.dtos.TutorRequest;
import com.mdd.booktor.dtos.TutorResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.DatoInvalidoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.Adaptacion;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.models.Reserva;
import com.mdd.booktor.repositories.AdaptacionRepository;
import com.mdd.booktor.repositories.ReservaRepository;
import com.mdd.booktor.repositories.TurnoRepository;
import com.mdd.booktor.repositories.UsuarioRepository;
import com.mdd.booktor.services.TutorService;
import com.mdd.booktor.utils.TutorMapper;

@Service
public class TutorServiceImpl implements TutorService {

    private final UsuarioRepository usuarioRepository;
    private final AdaptacionRepository adaptacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TurnoRepository turnoRepository;
    private final ReservaRepository reservaRepository;
    private final Clock clock;

    public TutorServiceImpl(UsuarioRepository usuarioRepository, AdaptacionRepository adaptacionRepository,
            PasswordEncoder passwordEncoder, TurnoRepository turnoRepository, ReservaRepository reservaRepository, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.adaptacionRepository = adaptacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.turnoRepository = turnoRepository;
        this.reservaRepository = reservaRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorResponse> listar() {
        return usuarioRepository.findByRolAndActivoTrue(Rol.TUTOR).stream()
                .map(TutorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TutorResponse buscarPorId(Long id) {
        return TutorMapper.toResponse(buscarTutorActivo(id));
    }

    @Override
    @Transactional
    public TutorResponse crear(TutorRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new DatoInvalidoException("La contraseña es obligatoria para dar de alta un tutor.");
        }

        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictoException("Ya existe un usuario con el email [" + email + "], vuelva a intentar con otro email.");
        }

        Set<Adaptacion> adaptaciones = buscarAdaptacionesActivas(request.adaptacionIds());

        Usuario tutor = new Usuario(
            request.nombre().trim(),
            request.apellido().trim(),
            email,
            passwordEncoder.encode(request.password()),
            Rol.TUTOR,
            true
        );
        tutor.setDuracionTurnoMinutos(request.duracionTurnoMinutos());
        tutor.setAdaptaciones(adaptaciones);

        return TutorMapper.toResponse(usuarioRepository.save(tutor));
    }

    @Override
    @Transactional
    public TutorResponse modificar(Long id, TutorRequest request) {
        Usuario tutor = buscarTutorActivo(id);

        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictoException("Ya existe un usuario con el email [" + email + "], vuelva a intentar con otro email.");
        }

        if (!request.duracionTurnoMinutos().equals(tutor.getDuracionTurnoMinutos())
                && turnoRepository.existsByTutor_IdAndEstadoAndInicioAfter(id, EstadoTurno.ASIGNADO, LocalDateTime.now(clock))) {
            throw new ConflictoException("No se puede cambiar la duración del turno, el tutor tiene turnos futuros asignados.");
        }

        Set<Adaptacion> adaptaciones = buscarAdaptacionesActivas(request.adaptacionIds());

        tutor.setNombre(request.nombre().trim());
        tutor.setApellido(request.apellido().trim());
        tutor.setEmail(email);
        if (request.password() != null && !request.password().isBlank()) {
            tutor.setPassword(passwordEncoder.encode(request.password()));
        }
        tutor.setDuracionTurnoMinutos(request.duracionTurnoMinutos());
        tutor.setAdaptaciones(adaptaciones);

        return TutorMapper.toResponse(usuarioRepository.save(tutor));
    }

    @Override
    @Transactional
    public void darDeBaja(Long id) {
        Usuario tutor = buscarTutorActivo(id);

        List<Reserva> reservas = reservaRepository.buscarActivasFuturasDeTutor(id, LocalDateTime.now(clock));
        if (!reservas.isEmpty()) {
            throw new ConflictoException("No se puede dar de baja al tutor, primero hay que cancelar las reservas activas de sus turnos: "
                    + describir(reservas) + ".");
        }

        tutor.setActivo(false);
        usuarioRepository.save(tutor);
    }

    private Usuario buscarTutorActivo(Long id) {
        return usuarioRepository.findByIdAndRolAndActivoTrue(id, Rol.TUTOR)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un tutor activo con el id [" + id + "]."));
    }

    private String describir(List<Reserva> reservas) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return reservas.stream()
                .map(r -> "[" + r.getId() + "] " + r.getTurno().getMateria().getNombre() + " " + r.getTurno().getInicio().format(formato))
                .collect(Collectors.joining(", "));
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
