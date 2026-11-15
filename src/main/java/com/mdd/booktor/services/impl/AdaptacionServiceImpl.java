package com.mdd.booktor.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mdd.booktor.dtos.AdaptacionRequest;
import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.Adaptacion;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.AdaptacionRepository;
import com.mdd.booktor.repositories.UsuarioRepository;

import com.mdd.booktor.services.AdaptacionService;
import com.mdd.booktor.utils.AdaptacionMapper;

@Service
public class AdaptacionServiceImpl implements AdaptacionService {

    private final AdaptacionRepository repository;
    private final UsuarioRepository usuarioRepository;

    public AdaptacionServiceImpl(AdaptacionRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override 
    public List<AdaptacionResponse> listar() {
        List<Adaptacion> adaptaciones = repository.findByActivoTrue();
        return adaptaciones.stream()
                .map(AdaptacionMapper::toResponse)
                .toList();
    }

    @Override
    public AdaptacionResponse buscarPorId(Long id) {
        Adaptacion adaptacion = repository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe una adaptacion activa con el id ["+id+"]."));
        return AdaptacionMapper.toResponse(adaptacion);
    }

    @Override
    public AdaptacionResponse crear(AdaptacionRequest request) {
        String nombre = request.nombre().trim();

        if (repository.existsByNombreIgnoreCaseAndActivoTrue(nombre)) {
            throw new ConflictoException("La adaptacion con nombre ["+nombre+"] ya existe, vuelva a intentar con otro nombre.");
        }

        Adaptacion adaptacion = AdaptacionMapper.toEntity(request);
        adaptacion.setNombre(nombre);
        Adaptacion guardada = repository.save(adaptacion);
        return AdaptacionMapper.toResponse(guardada);
    }

    @Override
    public AdaptacionResponse modificar(Long id, AdaptacionRequest request) {
        Adaptacion adaptacion = repository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe una adaptacion activa con el id ["+id+"]."));

        String nombre = request.nombre().trim();

        if (repository.existsByNombreIgnoreCaseAndActivoTrueAndIdNot(nombre, id)) {
            throw new ConflictoException("La adaptación con nombre ["+nombre+"] ya existe, vuelva a intentar con otro nombre.");
        }

        adaptacion.setNombre(nombre);
        adaptacion.setDescripcion(request.descripcion());
        Adaptacion actualizada = repository.save(adaptacion);
        return AdaptacionMapper.toResponse(actualizada);
    }

    @Override
    public void darDeBaja(Long id) {
        Adaptacion adaptacion = repository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe una adaptacion activa con el id ["+id+"]."));

        List<Usuario> tutores = usuarioRepository.findByRolAndActivoTrueAndAdaptaciones_Id(Rol.TUTOR, id);
        if (!tutores.isEmpty()) {
            String nombres = tutores.stream()
                    .map(t -> t.getNombre() + " " + t.getApellido())
                    .collect(Collectors.joining(", "));
            throw new ConflictoException("No se puede dar de baja la adaptacion, la usan " + tutores.size() + " tutores activos: " + nombres + ".");
        }

        adaptacion.setActivo(false);
        repository.save(adaptacion);
    }
}