package com.mdd.booktor.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mdd.booktor.dtos.AdaptacionRequest;
import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.Adaptacion;
import com.mdd.booktor.repositories.AdaptacionRepository;

@ExtendWith(MockitoExtension.class)
class AdaptacionServiceImplTest {

    @Mock
    private AdaptacionRepository repository;

    @InjectMocks
    private AdaptacionServiceImpl service;

    @Test
    void crearAdaptacionValida() {
        when(repository.existsByNombreIgnoreCaseAndActivoTrue("Material visual")).thenReturn(false);
        Adaptacion guardada = new Adaptacion("Material visual", "Imágenes y diagramas", true);
        when(repository.save(any(Adaptacion.class))).thenReturn(guardada);

        AdaptacionResponse respuesta = service.crear(new AdaptacionRequest("  Material visual  ", "Imágenes y diagramas"));

        // Captura la adaptación que el service le pasó a save, para revisar cómo la armó
        ArgumentCaptor<Adaptacion> captor = ArgumentCaptor.forClass(Adaptacion.class);
        verify(repository).save(captor.capture());
        assertEquals("Material visual", captor.getValue().getNombre());
        assertTrue(captor.getValue().isActivo());

        assertEquals("Material visual", respuesta.nombre());
        assertEquals("Imágenes y diagramas", respuesta.descripcion());
    }

    @Test
    void crearConNombreRepetidoLanza409() {
        when(repository.existsByNombreIgnoreCaseAndActivoTrue("Material visual")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.crear(new AdaptacionRequest("Material visual", null)));

        verify(repository, never()).save(any());
    }

    @Test
    void buscarPorIdDevuelveLaAdaptacion() {
        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(new Adaptacion("Material visual", "Imágenes", true)));

        AdaptacionResponse respuesta = service.buscarPorId(1L);

        assertEquals("Material visual", respuesta.nombre());
    }

    @Test
    void buscarPorIdInexistenteLanza404() {
        when(repository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void modificarInexistenteLanza404() {
        when(repository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.modificar(99L, new AdaptacionRequest("Lectura fácil", null)));

        verify(repository, never()).save(any());
    }

    @Test
    void modificarConNombreDeOtraLanza409() {
        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(new Adaptacion("Material visual", "Imágenes", true)));
        when(repository.existsByNombreIgnoreCaseAndActivoTrueAndIdNot("Lectura fácil", 1L)).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.modificar(1L, new AdaptacionRequest("Lectura fácil", null)));

        verify(repository, never()).save(any());
    }

    @Test
    void modificarManteniendoSuNombreFunciona() {
        // Caso límite: la propia adaptación no cuenta como nombre repetido
        Adaptacion existente = new Adaptacion("Material visual", "Imágenes", true);
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByNombreIgnoreCaseAndActivoTrueAndIdNot("Material visual", 1L)).thenReturn(false);
        when(repository.save(existente)).thenReturn(existente);

        AdaptacionResponse respuesta = service.modificar(1L, new AdaptacionRequest("Material visual", "Imágenes y subtítulos"));

        assertEquals("Material visual", respuesta.nombre());
        assertEquals("Imágenes y subtítulos", respuesta.descripcion());
    }

    @Test
    void darDeBajaPoneActivoEnFalse() {
        Adaptacion adaptacion = new Adaptacion("Material visual", "Imágenes", true);
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(adaptacion));

        service.darDeBaja(1L);

        // Baja lógica: se guarda con activo en false, no se borra
        assertFalse(adaptacion.isActivo());
        verify(repository).save(adaptacion);
    }

    @Test
    void darDeBajaInexistenteLanza404() {
        when(repository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.darDeBaja(99L));

        verify(repository, never()).save(any());
    }

    @Test
    void listarDevuelveLasActivas() {
        when(repository.findByActivoTrue()).thenReturn(List.of(
                new Adaptacion("Material visual", "Imágenes", true),
                new Adaptacion("Lectura fácil", "Textos simples", true)));

        List<AdaptacionResponse> respuesta = service.listar();

        assertEquals(2, respuesta.size());
        assertEquals("Lectura fácil", respuesta.get(1).nombre());
    }
}
