package com.mdd.booktor.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.dtos.TurnoDisponibleResponse;
import com.mdd.booktor.dtos.TutorDisponibleResponse;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.services.BusquedaService;

// Pruebas del controller de búsqueda (#12): disponibilidad en JSON y filtros.
@WebMvcTest(BusquedaController.class)
class BusquedaControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 20, 14, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BusquedaService busquedaService;

    @Test
    void buscarMateriasPorNombreDevuelve200() throws Exception {
        when(busquedaService.buscarMaterias("mate")).thenReturn(List.of(
                new MateriaResponse(2L, "Matemática", "Análisis I")));

        mockMvc.perform(get("/api/busqueda/materias").param("nombre", "mate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Matemática"));
    }

    @Test
    void buscarTutoresDevuelveSuDisponibilidadSinElLink() throws Exception {
        when(busquedaService.buscarTutores(2L, null, null)).thenReturn(List.of(
                new TutorDisponibleResponse(1L, "Juan", "Gómez",
                        List.of(new AdaptacionResponse(3L, "Material visual", "Gráficos y esquemas")),
                        List.of(new TurnoDisponibleResponse(10L, INICIO, INICIO.plusHours(1), Modalidad.VIRTUAL)))));

        mockMvc.perform(get("/api/busqueda/materias/2/tutores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[0].adaptaciones[0].nombre").value("Material visual"))
                .andExpect(jsonPath("$[0].turnos[0].inicio").value("2026-11-20T14:00:00"))
                .andExpect(jsonPath("$[0].turnos[0].modalidad").value("VIRTUAL"))
                // RN-28: el link de la reunión solo lo ve quien reservó, no aparece en la búsqueda.
                .andExpect(jsonPath("$[0].turnos[0].link").doesNotExist());
    }

    @Test
    void losFiltrosDeAdaptacionesYModalidadLleganAlService() throws Exception {
        when(busquedaService.buscarTutores(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/busqueda/materias/2/tutores")
                        .param("adaptaciones", "3,4")
                        .param("modalidad", "PRESENCIAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Comprueba que Spring convirtió "3,4" en un Set y "PRESENCIAL" en el enum.
        verify(busquedaService).buscarTutores(eq(2L), eq(Set.of(3L, 4L)), eq(Modalidad.PRESENCIAL));
    }

    @Test
    void modalidadInexistenteDevuelve400() throws Exception {
        mockMvc.perform(get("/api/busqueda/materias/2/tutores").param("modalidad", "HIBRIDA"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(busquedaService);
    }

    @Test
    void materiaInexistenteDevuelve404() throws Exception {
        String mensaje = "No existe una materia activa con el id [99].";
        when(busquedaService.buscarTutores(eq(99L), any(), any())).thenThrow(new RecursoNoEncontradoException(mensaje));

        mockMvc.perform(get("/api/busqueda/materias/99/tutores"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }
}
