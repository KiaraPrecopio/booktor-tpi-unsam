package com.mdd.booktor.services.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mdd.booktor.dtos.BajaTurnoRequest;
import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.dtos.TurnoTutorResponse;
import com.mdd.booktor.exceptions.AccesoDenegadoException;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.DatoInvalidoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Materia;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Reserva;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.MateriaRepository;
import com.mdd.booktor.repositories.ReservaRepository;
import com.mdd.booktor.repositories.TurnoRepository;
import com.mdd.booktor.repositories.UsuarioRepository;
import com.mdd.booktor.services.TurnoService;
import com.mdd.booktor.utils.TurnoMapper;

@Service
public class TurnoServiceImpl implements TurnoService {

    private static final DateTimeFormatter FORMATO_INICIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FIN = DateTimeFormatter.ofPattern("HH:mm");

    private final TurnoRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MateriaRepository materiaRepository;
    private final ReservaRepository reservaRepository;
    private final Clock clock;

    public TurnoServiceImpl(TurnoRepository turnoRepository, UsuarioRepository usuarioRepository,
            MateriaRepository materiaRepository, ReservaRepository reservaRepository, Clock clock) {
        this.turnoRepository = turnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.materiaRepository = materiaRepository;
        this.reservaRepository = reservaRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public List<TurnoResponse> generarBloque(BloqueTurnosRequest request) {
        Usuario tutor = usuarioRepository.findByIdAndRolAndActivoTrue(request.tutorId(), Rol.TUTOR)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un tutor activo con el id [" + request.tutorId() + "]."));

        Materia materia = materiaRepository.findByIdAndActivoTrue(request.materiaId())
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia activa con el id [" + request.materiaId() + "]."));

        if (!request.horaHasta().isAfter(request.horaDesde())) {
            throw new DatoInvalidoException("La hora hasta tiene que ser posterior a la hora desde.");
        }

        LocalDateTime inicioBloque = request.fecha().atTime(request.horaDesde());
        LocalDateTime finBloque = request.fecha().atTime(request.horaHasta());

        if (inicioBloque.isBefore(LocalDateTime.now(clock))) {
            throw new DatoInvalidoException("No se pueden generar turnos en el pasado.");
        }

        int duracion = tutor.getDuracionTurnoMinutos();
        long minutosBloque = Duration.between(inicioBloque, finBloque).toMinutes();
        if (minutosBloque % duracion != 0) {
            throw new DatoInvalidoException("El bloque dura " + minutosBloque + " minutos y tiene que ser múltiplo de "
                    + duracion + " minutos, que es la duración de los turnos del tutor.");
        }

        String link = null;
        String lugar = null;
        if (request.modalidad() == Modalidad.VIRTUAL) {
            if (request.link() == null || request.link().isBlank()) {
                throw new DatoInvalidoException("El link de la reunión es obligatorio para los turnos virtuales.");
            }
            link = request.link().trim();
        } else {
            if (request.lugar() == null || request.lugar().isBlank()) {
                throw new DatoInvalidoException("El lugar es obligatorio para los turnos presenciales.");
            }
            lugar = request.lugar().trim();
        }

        List<Turno> superpuestos = turnoRepository.buscarSuperpuestos(request.tutorId(), inicioBloque, finBloque);
        if (!superpuestos.isEmpty()) {
            String horarios = superpuestos.stream()
                    .map(t -> t.getInicio().format(FORMATO_INICIO) + "-" + t.getFin().format(FORMATO_FIN))
                    .collect(Collectors.joining(", "));
            throw new ConflictoException("El bloque se superpone con estos turnos del tutor: " + horarios + ".");
        }

        List<Turno> turnos = new ArrayList<>();
        for (LocalDateTime inicio = inicioBloque; inicio.isBefore(finBloque); inicio = inicio.plusMinutes(duracion)) {
            turnos.add(new Turno(tutor, materia, inicio, inicio.plusMinutes(duracion), request.modalidad(), link, lugar));
        }

        return turnoRepository.saveAll(turnos).stream()
                .map(TurnoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoTutorResponse> listarProximosDeTutor(Long tutorId) {
        usuarioRepository.findByIdAndRolAndActivoTrue(tutorId, Rol.TUTOR)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un tutor activo con el id [" + tutorId + "]."));

        LocalDateTime ahora = LocalDateTime.now(clock);
        List<Turno> turnos = turnoRepository.buscarFuturosDeTutor(tutorId,
                List.of(EstadoTurno.DISPONIBLE, EstadoTurno.ASIGNADO), ahora);

        Map<Turno, Reserva> reservaPorTurno = new HashMap<>();
        for (Reserva reserva : reservaRepository.buscarActivasFuturasDeTutor(tutorId, ahora)) {
            reservaPorTurno.put(reserva.getTurno(), reserva);
        }

        return turnos.stream()
                .map(t -> toTurnoTutorResponse(t, reservaPorTurno.get(t)))
                .toList();
    }

    @Override
    @Transactional
    public TurnoResponse darDeBaja(Long id, BajaTurnoRequest request) {
        Turno turno = turnoRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un turno con el id [" + id + "]."));

        if (!Objects.equals(turno.getTutor().getId(), request.tutorId())) {
            throw new AccesoDenegadoException("Solo el tutor del turno puede darlo de baja.");
        }

        if (turno.getEstado() == EstadoTurno.ASIGNADO) {
            String reserva = reservaRepository.findByTurno_IdAndEstado(id, EstadoReserva.ACTIVA)
                    .map(r -> "[" + r.getId() + "] de " + r.getEstudiante().getNombre() + " " + r.getEstudiante().getApellido())
                    .orElse("activa");
            throw new ConflictoException("No se puede dar de baja el turno, primero hay que cancelar la reserva " + reserva + ".");
        }

        if (turno.getEstado() != EstadoTurno.DISPONIBLE) {
            throw new ConflictoException("Solo se pueden dar de baja turnos disponibles.");
        }

        turno.setEstado(EstadoTurno.DADO_DE_BAJA);
        turnoRepository.save(turno);
        return TurnoMapper.toResponse(turno);
    }

    private TurnoTutorResponse toTurnoTutorResponse(Turno turno, Reserva reserva) {
        return new TurnoTutorResponse(
            turno.getId(),
            turno.getMateria().getNombre(),
            turno.getInicio(),
            turno.getFin(),
            turno.getEstado(),
            turno.getModalidad(),
            turno.getLink(),
            turno.getLugar(),
            reserva == null ? null : reserva.getId(),
            reserva == null ? null : reserva.getEstudiante().getNombre() + " " + reserva.getEstudiante().getApellido(),
            reserva == null ? null : reserva.getObjetivo()
        );
    }
}
