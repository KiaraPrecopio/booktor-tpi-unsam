package com.mdd.booktor.services.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.dtos.TurnoDisponibleResponse;
import com.mdd.booktor.dtos.TutorDisponibleResponse;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.Adaptacion;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.MateriaRepository;
import com.mdd.booktor.repositories.TurnoRepository;
import com.mdd.booktor.services.BusquedaService;
import com.mdd.booktor.utils.AdaptacionMapper;
import com.mdd.booktor.utils.MateriaMapper;

@Service
public class BusquedaServiceImpl implements BusquedaService {

    private final MateriaRepository materiaRepository;
    private final TurnoRepository turnoRepository;
    private final Clock clock;

    public BusquedaServiceImpl(MateriaRepository materiaRepository, TurnoRepository turnoRepository, Clock clock) {
        this.materiaRepository = materiaRepository;
        this.turnoRepository = turnoRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MateriaResponse> buscarMaterias(String nombre) {
        String texto = nombre == null ? "" : nombre.trim();
        return materiaRepository.buscarConTurnosDisponibles(texto, LocalDateTime.now(clock)).stream()
                .map(MateriaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorDisponibleResponse> buscarTutores(Long materiaId, Set<Long> adaptacionIds, Modalidad modalidad) {
        materiaRepository.findByIdAndActivoTrue(materiaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia activa con el id [" + materiaId + "]."));

        List<Turno> turnos = turnoRepository.buscarDisponiblesDeMateria(materiaId, LocalDateTime.now(clock));

        Map<Usuario, List<Turno>> turnosPorTutor = new LinkedHashMap<>();
        for (Turno turno : turnos) {
            if (modalidad != null && turno.getModalidad() != modalidad) {
                continue;
            }
            if (!ofreceTodas(turno.getTutor(), adaptacionIds)) {
                continue;
            }
            turnosPorTutor.computeIfAbsent(turno.getTutor(), t -> new ArrayList<>()).add(turno);
        }

        return turnosPorTutor.entrySet().stream()
                .map(e -> toResponse(e.getKey(), e.getValue()))
                .toList();
    }

    private boolean ofreceTodas(Usuario tutor, Set<Long> adaptacionIds) {
        if (adaptacionIds == null || adaptacionIds.isEmpty()) {
            return true;
        }
        Set<Long> ofrecidas = tutor.getAdaptaciones().stream()
                .map(Adaptacion::getId)
                .collect(Collectors.toSet());
        return ofrecidas.containsAll(adaptacionIds);
    }

    private TutorDisponibleResponse toResponse(Usuario tutor, List<Turno> turnos) {
        return new TutorDisponibleResponse(
            tutor.getId(),
            tutor.getNombre(),
            tutor.getApellido(),
            tutor.getAdaptaciones().stream()
                    .sorted(Comparator.comparing(Adaptacion::getNombre))
                    .map(AdaptacionMapper::toResponse)
                    .toList(),
            turnos.stream()
                    .map(t -> new TurnoDisponibleResponse(t.getId(), t.getInicio(), t.getFin(), t.getModalidad()))
                    .toList()
        );
    }
}
