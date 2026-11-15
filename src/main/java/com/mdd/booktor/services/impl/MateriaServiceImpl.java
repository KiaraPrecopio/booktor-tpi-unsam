package com.mdd.booktor.services.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mdd.booktor.dtos.MateriaRequest;
import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Materia;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.repositories.MateriaRepository;
import com.mdd.booktor.repositories.TurnoRepository;
import com.mdd.booktor.services.MateriaService;
import com.mdd.booktor.utils.MateriaMapper;

@Service 
public class MateriaServiceImpl implements MateriaService {

    private final MateriaRepository repository;
    private final TurnoRepository turnoRepository;
    private final Clock clock;

    public MateriaServiceImpl(MateriaRepository repository, TurnoRepository turnoRepository, Clock clock) {
        this.repository = repository;
        this.turnoRepository = turnoRepository;
        this.clock = clock;
    }

    @Override
    public List<MateriaResponse> listar() {
        List<Materia> materias = repository.findByActivoTrue();
        return materias.stream()
                .map(MateriaMapper::toResponse)
                .toList();
    }

    @Override
    public MateriaResponse buscarPorId(Long id) {
        Materia materia = repository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia activa con el id ["+id+"]."));
        return MateriaMapper.toResponse(materia);
    }

    @Override
    public MateriaResponse crear(MateriaRequest request) {
        String nombre = request.nombre().trim();

        if (repository.existsByNombreIgnoreCaseAndActivoTrue(nombre)) {
            throw new ConflictoException("La materia con nombre ["+nombre+"] ya existe, vuelva a intentar con otro nombre.");
        }

        Materia materia = MateriaMapper.toEntity(request);
        materia.setNombre(nombre);
        Materia guardada = repository.save(materia);
        return MateriaMapper.toResponse(guardada);
    }

    @Override
    public MateriaResponse modificar(Long id, MateriaRequest request) {
        Materia materia = repository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia activa con el id ["+id+"]."));

        String nombre = request.nombre().trim();

        if (repository.existsByNombreIgnoreCaseAndActivoTrueAndIdNot(nombre, id)) {
            throw new ConflictoException("La materia con nombre ["+nombre+"] ya existe, vuelva a intentar con otro nombre.");
        }

        materia.setNombre(nombre);
        materia.setDescripcion(request.descripcion());

        Materia actualizada = repository.save(materia);
        return MateriaMapper.toResponse(actualizada);
    }

    @Override
    public void darDeBaja(Long id) {
        Materia materia = repository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia activa con el id ["+id+"]."));

        List<Turno> turnos = turnoRepository.buscarFuturosDeMateria(id,
                List.of(EstadoTurno.DISPONIBLE, EstadoTurno.ASIGNADO), LocalDateTime.now(clock));
        if (!turnos.isEmpty()) {
            String tutores = turnos.stream()
                    .map(t -> t.getTutor().getNombre() + " " + t.getTutor().getApellido())
                    .distinct()
                    .collect(Collectors.joining(", "));
            throw new ConflictoException("No se puede dar de baja la materia, tiene " + turnos.size() + " turnos futuros de los tutores: " + tutores + ".");
        }

        materia.setActivo(false);
        repository.save(materia);
    }
    
}
