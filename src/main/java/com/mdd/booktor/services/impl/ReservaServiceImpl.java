package com.mdd.booktor.services.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mdd.booktor.dtos.CancelacionRequest;
import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.exceptions.AccesoDenegadoException;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Reserva;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.ReservaRepository;
import com.mdd.booktor.repositories.TurnoRepository;
import com.mdd.booktor.repositories.UsuarioRepository;
import com.mdd.booktor.services.ReservaService;
import com.mdd.booktor.utils.ReservaMapper;

@Service
public class ReservaServiceImpl implements ReservaService {

    private static final int MINUTOS_MINIMOS_ANTES = 5;
    private static final DateTimeFormatter FORMATO_INICIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FIN = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservaRepository reservaRepository;
    private final TurnoRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public ReservaServiceImpl(ReservaRepository reservaRepository, TurnoRepository turnoRepository,
            UsuarioRepository usuarioRepository, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.turnoRepository = turnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ReservaResponse reservar(ReservaRequest request) {
        Usuario estudiante = usuarioRepository.findByIdAndRolAndActivoTrue(request.estudianteId(), Rol.ESTUDIANTE)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un estudiante activo con el id [" + request.estudianteId() + "]."));

        Turno turno = turnoRepository.findById(request.turnoId())
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un turno con el id [" + request.turnoId() + "]."));

        if (turno.getEstado() != EstadoTurno.DISPONIBLE) {
            throw new ConflictoException("El turno no está disponible para reservar.");
        }

        if (!turno.getTutor().isActivo()) {
            throw new ConflictoException("El tutor del turno no está activo.");
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        if (ahora.isAfter(turno.getInicio().minusMinutes(MINUTOS_MINIMOS_ANTES))) {
            throw new ConflictoException("Solo se puede reservar hasta " + MINUTOS_MINIMOS_ANTES + " minutos antes del inicio del turno.");
        }

        List<Reserva> superpuestas = reservaRepository.buscarActivasSuperpuestas(request.estudianteId(), turno.getInicio(), turno.getFin());
        if (!superpuestas.isEmpty()) {
            String turnos = superpuestas.stream()
                    .map(r -> r.getTurno().getMateria().getNombre() + " " + r.getTurno().getInicio().format(FORMATO_INICIO)
                            + "-" + r.getTurno().getFin().format(FORMATO_FIN))
                    .collect(Collectors.joining(", "));
            throw new ConflictoException("El estudiante ya tiene una reserva que se superpone con este turno: " + turnos + ".");
        }

        String objetivo = request.objetivo() == null || request.objetivo().isBlank() ? null : request.objetivo().trim();

        Reserva reserva = new Reserva(turno, estudiante, ahora, objetivo);
        turno.setEstado(EstadoTurno.ASIGNADO);
        try {
            reserva = reservaRepository.saveAndFlush(reserva);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictoException("Este turno acaba de ser reservado por otra persona.");
        }
        turnoRepository.save(turno);

        return ReservaMapper.toResponse(reserva);
    }

    @Override
    @Transactional
    public ReservaResponse cancelar(Long id, CancelacionRequest request) {
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reserva con el id [" + id + "]."));

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
            .filter(Usuario::isActivo)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario activo con el id [" + request.usuarioId() + "]."));

        Turno turno = reserva.getTurno();
        boolean puedeCancelar = switch (usuario.getRol()) {
            case ADMINISTRADOR -> true;
            case ESTUDIANTE -> Objects.equals(reserva.getEstudiante().getId(), usuario.getId());
            case TUTOR -> Objects.equals(turno.getTutor().getId(), usuario.getId());
        };
        if (!puedeCancelar) {
            throw new AccesoDenegadoException("Solo pueden cancelar la reserva el estudiante que reservó, el tutor del turno o un administrador.");
        }

        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new ConflictoException("Solo se pueden cancelar reservas activas.");
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        if (!ahora.isBefore(turno.getInicio())) {
            throw new ConflictoException("No se puede cancelar la reserva de un turno que ya empezó.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCanceladaPor(usuario.getRol());
        reserva.setFechaCancelacion(ahora);
        turno.setEstado(EstadoTurno.DISPONIBLE);
        reservaRepository.save(reserva);
        turnoRepository.save(turno);

        return ReservaMapper.toResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarActivasFuturas() {
        return reservaRepository.buscarActivasFuturas(LocalDateTime.now(clock)).stream()
                .map(ReservaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarProximasDeEstudiante(Long estudianteId) {
        usuarioRepository.findByIdAndRolAndActivoTrue(estudianteId, Rol.ESTUDIANTE)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un estudiante activo con el id [" + estudianteId + "]."));

        return reservaRepository.buscarActivasFuturasDeEstudiante(estudianteId, LocalDateTime.now(clock)).stream()
                .map(ReservaMapper::toResponse)
                .toList();
    }
}
