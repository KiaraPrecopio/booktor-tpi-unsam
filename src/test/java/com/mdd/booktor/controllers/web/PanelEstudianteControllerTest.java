package com.mdd.booktor.controllers.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mdd.booktor.configs.SecurityConfig;
import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.dtos.TurnoDisponibleResponse;
import com.mdd.booktor.dtos.TutorDisponibleResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.services.AdaptacionService;
import com.mdd.booktor.services.BusquedaService;
import com.mdd.booktor.services.MateriaService;
import com.mdd.booktor.services.ReservaService;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PanelEstudianteController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "juan@mail.com", roles = "ESTUDIANTE")
class PanelEstudianteControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 20, 14, 0);
    private static final AdaptacionResponse DISLEXIA = new AdaptacionResponse(3L, "Dislexia", null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BusquedaService busquedaService;
    @MockitoBean
    private MateriaService materiaService;
    @MockitoBean
    private AdaptacionService adaptacionService;
    @MockitoBean
    private ReservaService reservaService;

    private ReservaResponse reserva() {
        return new ReservaResponse(7L, 10L, 1L, "Juan Pérez", "Análisis I", "Ana Gómez", INICIO, INICIO.plusHours(1),
                Modalidad.VIRTUAL, "https://meet/abc", null, EstadoReserva.ACTIVA, INICIO.minusDays(1), "Repasar", null, null);
    }

    @Test
    void elMenuTieneLosLinksDelEstudiante() throws Exception {
        mockMvc.perform(get("/estudiante"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/estudiante/buscar")))
                .andExpect(content().string(containsString("/estudiante/reservas")));
    }

    @Test
    void buscarMuestraLasMateriasConTurnos() throws Exception {
        when(busquedaService.buscarMaterias("anal")).thenReturn(List.of(new MateriaResponse(2L, "Análisis I", "Límites")));

        mockMvc.perform(get("/estudiante/buscar").param("nombre", "anal"))
                .andExpect(view().name("estudiante/buscar"))
                .andExpect(content().string(containsString("Análisis I")));
    }

    @Test
    void tutoresMuestraLosTurnosConLosFiltrosElegidos() throws Exception {
        when(materiaService.buscarPorId(2L)).thenReturn(new MateriaResponse(2L, "Análisis I", null));
        when(adaptacionService.listar()).thenReturn(List.of(DISLEXIA));
        when(busquedaService.buscarTutores(2L, Set.of(3L), Modalidad.VIRTUAL)).thenReturn(List.of(
                new TutorDisponibleResponse(5L, "Ana", "Gómez", List.of(DISLEXIA),
                        List.of(new TurnoDisponibleResponse(10L, INICIO, INICIO.plusHours(1), Modalidad.VIRTUAL)))));

        mockMvc.perform(get("/estudiante/tutores").param("materiaId", "2").param("adaptaciones", "3").param("modalidad", "VIRTUAL"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana Gómez")))
                .andExpect(content().string(containsString("20/11/2026 14:00")))
                .andExpect(content().string(containsString("checked")))
                .andExpect(content().string(containsString("name=\"turnoId\" value=\"10\"")));
    }

    @Test
    void sinFiltrosBuscaTodosLosTutoresDeLaMateria() throws Exception {
        when(materiaService.buscarPorId(2L)).thenReturn(new MateriaResponse(2L, "Análisis I", null));

        mockMvc.perform(get("/estudiante/tutores").param("materiaId", "2").param("modalidad", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay tutores")));

        verify(busquedaService).buscarTutores(2L, null, null);
    }

    @Test
    void reservarLoHaceANombreDelEstudianteDeLaSesion() throws Exception {
        mockMvc.perform(post("/estudiante/reservas").with(csrf()).param("turnoId", "10").param("objetivo", " Repasar "))
                .andExpect(redirectedUrl("/estudiante/reservas"));

        ArgumentCaptor<ReservaRequest> request = ArgumentCaptor.forClass(ReservaRequest.class);
        verify(reservaService).reservar(request.capture(), eq("juan@mail.com"));
        assertEquals(10L, request.getValue().turnoId());
        assertEquals("Repasar", request.getValue().objetivo());
    }

    @Test
    void elObjetivoVacioLlegaComoNull() throws Exception {
        mockMvc.perform(post("/estudiante/reservas").with(csrf()).param("turnoId", "10").param("objetivo", ""))
                .andExpect(redirectedUrl("/estudiante/reservas"));

        ArgumentCaptor<ReservaRequest> request = ArgumentCaptor.forClass(ReservaRequest.class);
        verify(reservaService).reservar(request.capture(), eq("juan@mail.com"));
        assertNull(request.getValue().objetivo());
    }

    @Test
    void unObjetivoDemasiadoLargoMuestraElError() throws Exception {
        mockMvc.perform(post("/estudiante/reservas").with(csrf()).param("turnoId", "10").param("objetivo", "a".repeat(501)))
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("El objetivo no puede superar los 500 caracteres")));

        verifyNoInteractions(reservaService);
    }

    @Test
    void siElServiceRechazaLaReservaSeMuestraElMotivo() throws Exception {
        when(reservaService.reservar(any(), any())).thenThrow(new ConflictoException("El turno ya no está disponible."));

        mockMvc.perform(post("/estudiante/reservas").with(csrf()).param("turnoId", "10"))
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("El turno ya no está disponible.")));
    }

    @Test
    void misReservasMuestraLasDelEstudianteDeLaSesion() throws Exception {
        when(reservaService.listarProximasDeEstudiante("juan@mail.com")).thenReturn(List.of(reserva()));

        mockMvc.perform(get("/estudiante/reservas"))
                .andExpect(content().string(containsString("Ana Gómez")))
                .andExpect(content().string(containsString("https://meet/abc")))
                .andExpect(content().string(containsString("name=\"reservaId\" value=\"7\"")));
    }

    @Test
    void cancelarVuelveAMisReservas() throws Exception {
        mockMvc.perform(post("/estudiante/reservas/cancelar").with(csrf()).param("reservaId", "7"))
                .andExpect(redirectedUrl("/estudiante/reservas"));

        verify(reservaService).cancelar(7L, "juan@mail.com");
    }

    @Test
    void sinTokenCsrfNoSePuedeReservar() throws Exception {
        mockMvc.perform(post("/estudiante/reservas").param("turnoId", "10"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reservaService);
    }
}
