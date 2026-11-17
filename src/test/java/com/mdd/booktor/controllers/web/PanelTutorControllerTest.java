package com.mdd.booktor.controllers.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mdd.booktor.configs.SecurityConfig;
import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.dtos.TurnoTutorResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.services.MateriaService;
import com.mdd.booktor.services.ReservaService;
import com.mdd.booktor.services.TurnoService;

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

@WebMvcTest(PanelTutorController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "ana@mail.com", roles = "TUTOR")
class PanelTutorControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 20, 14, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TurnoService turnoService;
    @MockitoBean
    private MateriaService materiaService;
    @MockitoBean
    private ReservaService reservaService;

    @Test
    void elMenuTieneLosLinksDelTutor() throws Exception {
        mockMvc.perform(get("/tutor"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/tutor/turnos/nuevo")));
    }

    @Test
    void misTurnosOfreceDarDeBajaLosLibresYCancelarLosReservados() throws Exception {
        when(turnoService.listarProximosDeTutor("ana@mail.com")).thenReturn(List.of(
                new TurnoTutorResponse(10L, "Análisis I", INICIO, INICIO.plusHours(1), EstadoTurno.DISPONIBLE,
                        Modalidad.PRESENCIAL, null, "Aula 3", null, null, null),
                new TurnoTutorResponse(11L, "Análisis I", INICIO.plusHours(1), INICIO.plusHours(2), EstadoTurno.ASIGNADO,
                        Modalidad.PRESENCIAL, null, "Aula 3", 7L, "Juan Pérez", "Repasar")));

        mockMvc.perform(get("/tutor/turnos"))
                .andExpect(content().string(containsString("name=\"turnoId\" value=\"10\"")))
                .andExpect(content().string(containsString("name=\"reservaId\" value=\"7\"")))
                .andExpect(content().string(containsString("Juan Pérez")));
    }

    @Test
    void elFormularioDeBloqueListaLasMaterias() throws Exception {
        when(materiaService.listar()).thenReturn(List.of(new MateriaResponse(2L, "Análisis I", null)));

        mockMvc.perform(get("/tutor/turnos/nuevo"))
                .andExpect(view().name("tutor/nuevo-bloque"))
                .andExpect(content().string(containsString("Análisis I")));
    }

    @Test
    void generarBloqueArmaElPedidoConLosCamposDelFormulario() throws Exception {
        mockMvc.perform(post("/tutor/turnos").with(csrf())
                        .param("materiaId", "2").param("fecha", "2026-11-20")
                        .param("horaDesde", "14:00").param("horaHasta", "16:00")
                        .param("modalidad", "VIRTUAL").param("link", "https://meet/abc").param("lugar", ""))
                .andExpect(redirectedUrl("/tutor/turnos"));

        ArgumentCaptor<BloqueTurnosRequest> request = ArgumentCaptor.forClass(BloqueTurnosRequest.class);
        verify(turnoService).generarBloque(request.capture(), eq("ana@mail.com"));
        assertEquals(LocalDate.of(2026, 11, 20), request.getValue().fecha());
        assertEquals(LocalTime.of(14, 0), request.getValue().horaDesde());
        assertEquals(Modalidad.VIRTUAL, request.getValue().modalidad());
        assertNull(request.getValue().lugar());
    }

    @Test
    void unBloqueSinMateriaMuestraElError() throws Exception {
        mockMvc.perform(post("/tutor/turnos").with(csrf()).param("fecha", "2026-11-20")
                        .param("horaDesde", "14:00").param("horaHasta", "16:00").param("modalidad", "VIRTUAL"))
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("La materia es obligatoria")));

        verifyNoInteractions(turnoService);
    }

    @Test
    void siElBloqueSeSuperponeSeMuestraElMotivo() throws Exception {
        when(turnoService.generarBloque(any(), any())).thenThrow(new ConflictoException("Se superpone con otro turno."));

        mockMvc.perform(post("/tutor/turnos").with(csrf()).param("materiaId", "2").param("fecha", "2026-11-20")
                        .param("horaDesde", "14:00").param("horaHasta", "16:00").param("modalidad", "VIRTUAL"))
                .andExpect(content().string(containsString("Se superpone con otro turno.")));
    }

    @Test
    void darDeBajaVuelveAMisTurnos() throws Exception {
        mockMvc.perform(post("/tutor/turnos/baja").with(csrf()).param("turnoId", "10"))
                .andExpect(redirectedUrl("/tutor/turnos"));

        verify(turnoService).darDeBaja(10L, "ana@mail.com");
    }

    @Test
    void cancelarUnaReservaVuelveAMisTurnos() throws Exception {
        mockMvc.perform(post("/tutor/reservas/cancelar").with(csrf()).param("reservaId", "7"))
                .andExpect(redirectedUrl("/tutor/turnos"));

        verify(reservaService).cancelar(7L, "ana@mail.com");
    }
}
