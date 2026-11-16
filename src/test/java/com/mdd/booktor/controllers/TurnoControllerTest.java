package com.mdd.booktor.controllers;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.dtos.TurnoTutorResponse;
import com.mdd.booktor.exceptions.AccesoDenegadoException;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.DatoInvalidoException;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.services.TurnoService;

// Pruebas del controller de turnos (#11 y la baja del #15).
@WebMvcTest(TurnoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TurnoControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 20, 14, 0);

    private static final String BLOQUE_VIRTUAL = """
            {"materiaId": 2, "fecha": "2026-11-20", "horaDesde": "14:00",
             "horaHasta": "16:00", "modalidad": "VIRTUAL", "link": "https://meet.google.com/abc"}
            """;

    private static final Principal TUTOR = () -> "ana@mail.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TurnoService turnoService;

    private TurnoResponse turno(Long id, LocalDateTime inicio, EstadoTurno estado) {
        return new TurnoResponse(id, 1L, 2L, "Matemática", inicio, inicio.plusHours(1), estado,
                Modalidad.VIRTUAL, "https://meet.google.com/abc", null);
    }

    @Test
    void generarBloqueDevuelve201ConLosTurnosCreados() throws Exception {
        when(turnoService.generarBloque(any(), any())).thenReturn(List.of(
                turno(10L, INICIO, EstadoTurno.DISPONIBLE),
                turno(11L, INICIO.plusHours(1), EstadoTurno.DISPONIBLE)));

        mockMvc.perform(post("/api/turnos/bloques").principal(TUTOR).contentType(MediaType.APPLICATION_JSON).content(BLOQUE_VIRTUAL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].inicio").value("2026-11-20T14:00:00"))
                .andExpect(jsonPath("$[1].inicio").value("2026-11-20T15:00:00"))
                .andExpect(jsonPath("$[0].estado").value("DISPONIBLE"));
    }

    @Test
    void bloqueSuperpuestoDevuelve409ConLosTurnosQueChocan() throws Exception {
        String mensaje = "El bloque se superpone con estos turnos del tutor: 20/11/2026 14:00 a 15:00.";
        when(turnoService.generarBloque(any(), any())).thenThrow(new ConflictoException(mensaje));

        mockMvc.perform(post("/api/turnos/bloques").principal(TUTOR).contentType(MediaType.APPLICATION_JSON).content(BLOQUE_VIRTUAL))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void bloqueVirtualSinLinkDevuelve400() throws Exception {
        String mensaje = "El link de la reunión es obligatorio para los turnos virtuales.";
        when(turnoService.generarBloque(any(), any())).thenThrow(new DatoInvalidoException(mensaje));

        mockMvc.perform(post("/api/turnos/bloques").principal(TUTOR).contentType(MediaType.APPLICATION_JSON).content("""
                        {"materiaId": 2, "fecha": "2026-11-20", "horaDesde": "14:00",
                         "horaHasta": "16:00", "modalidad": "VIRTUAL"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void bloqueSinDatosObligatoriosDevuelve400SinLlamarAlService() throws Exception {
        // Bean Validation (@NotNull del record) rechaza el pedido antes de entrar al controller.
        mockMvc.perform(post("/api/turnos/bloques").principal(TUTOR).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.materiaId").value("La materia es obligatoria"))
                .andExpect(jsonPath("$.errores.modalidad").value("La modalidad es obligatoria"));

        verifyNoInteractions(turnoService);
    }

    @Test
    void listarProximosDeTutorDevuelveElEstudianteYElObjetivo() throws Exception {
        when(turnoService.listarProximosDeTutor("ana@mail.com")).thenReturn(List.of(
                new TurnoTutorResponse(10L, "Matemática", INICIO, INICIO.plusHours(1), EstadoTurno.ASIGNADO,
                        Modalidad.VIRTUAL, "https://meet.google.com/abc", null, 5L, "Ana Pérez", "Repasar derivadas")));

        mockMvc.perform(get("/api/turnos/mis-turnos").principal(TUTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("ASIGNADO"))
                .andExpect(jsonPath("$[0].estudiante").value("Ana Pérez"))
                .andExpect(jsonPath("$[0].objetivo").value("Repasar derivadas"));
    }

    @Test
    void darDeBajaTurnoDisponibleDevuelve200() throws Exception {
        when(turnoService.darDeBaja(eq(10L), any())).thenReturn(turno(10L, INICIO, EstadoTurno.DADO_DE_BAJA));

        mockMvc.perform(patch("/api/turnos/10/baja").principal(TUTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DADO_DE_BAJA"));
    }

    @Test
    void darDeBajaTurnoAsignadoDevuelve409() throws Exception {
        String mensaje = "No se puede dar de baja el turno, primero hay que cancelar la reserva [7].";
        when(turnoService.darDeBaja(eq(10L), any())).thenThrow(new ConflictoException(mensaje));

        mockMvc.perform(patch("/api/turnos/10/baja").principal(TUTOR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void darDeBajaTurnoDeOtroTutorDevuelve403() throws Exception {
        String mensaje = "Solo el tutor del turno puede darlo de baja.";
        when(turnoService.darDeBaja(eq(10L), any())).thenThrow(new AccesoDenegadoException(mensaje));

        mockMvc.perform(patch("/api/turnos/10/baja").principal(TUTOR))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void elBloqueSeGeneraParaElTutorDeLaSesion() throws Exception {
        mockMvc.perform(post("/api/turnos/bloques").principal(TUTOR).contentType(MediaType.APPLICATION_JSON).content(BLOQUE_VIRTUAL))
                .andExpect(status().isCreated());

        verify(turnoService).generarBloque(any(), eq("ana@mail.com"));
    }
}
