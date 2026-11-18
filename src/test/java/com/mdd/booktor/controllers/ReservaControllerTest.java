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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.exceptions.AccesoDenegadoException;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.services.ReservaService;

// Pruebas del controller de reservas: reservar (#13), cancelar (#14) y los listados (#15).
@WebMvcTest(ReservaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservaControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 20, 14, 0);
    private static final String RESERVA = """
            {"turnoId": 10, "objetivo": "Repasar derivadas"}
            """;

    private static final Principal ESTUDIANTE = () -> "juan@mail.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService reservaService;

    // Arma una respuesta de ejemplo; solo cambian el estado y quién canceló.
    private ReservaResponse reserva(EstadoReserva estado, Rol canceladaPor) {
        LocalDateTime cancelacion = canceladaPor == null ? null : INICIO.minusDays(1);
        return new ReservaResponse(7L, 10L, 5L, "Ana Pérez", "Matemática", "Juan Gómez", INICIO, INICIO.plusHours(1),
                Modalidad.VIRTUAL, "https://meet.google.com/abc", null, estado, INICIO.minusDays(2),
                "Repasar derivadas", canceladaPor, cancelacion);
    }

    @Test
    void reservarDevuelve201ConLaUbicacionDeLaReserva() throws Exception {
        when(reservaService.reservar(any(), any())).thenReturn(reserva(EstadoReserva.ACTIVA, null));

        mockMvc.perform(post("/api/reservas").principal(ESTUDIANTE).contentType(MediaType.APPLICATION_JSON).content(RESERVA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/reservas/7"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.objetivo").value("Repasar derivadas"))
                .andExpect(jsonPath("$.link").value("https://meet.google.com/abc"));
    }

    @Test
    void reservarTurnoYaReservadoDevuelve409() throws Exception {
        String mensaje = "El turno no está disponible para reservar.";
        when(reservaService.reservar(any(), any())).thenThrow(new ConflictoException(mensaje));

        mockMvc.perform(post("/api/reservas").principal(ESTUDIANTE).contentType(MediaType.APPLICATION_JSON).content(RESERVA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void reservarTurnoSuperpuestoDevuelve409ConElTurnoQueChoca() throws Exception {
        String mensaje = "El estudiante ya tiene una reserva que se superpone con este turno: 20/11/2026 14:00 a 15:00.";
        when(reservaService.reservar(any(), any())).thenThrow(new ConflictoException(mensaje));

        mockMvc.perform(post("/api/reservas").principal(ESTUDIANTE).contentType(MediaType.APPLICATION_JSON).content(RESERVA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void reservarTurnoQueYaEmpezoDevuelve409() throws Exception {
        String mensaje = "Solo se puede reservar hasta 5 minutos antes del inicio del turno.";
        when(reservaService.reservar(any(), any())).thenThrow(new ConflictoException(mensaje));

        mockMvc.perform(post("/api/reservas").principal(ESTUDIANTE).contentType(MediaType.APPLICATION_JSON).content(RESERVA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void reservarConObjetivoDeMasDe500CaracteresDevuelve400() throws Exception {
        String objetivo = "a".repeat(501);

        mockMvc.perform(post("/api/reservas").principal(ESTUDIANTE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"turnoId\": 10, \"objetivo\": \"" + objetivo + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.objetivo").value("El objetivo no puede superar los 500 caracteres"));

        verifyNoInteractions(reservaService);
    }

    @Test
    void reservarSinTurnoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/reservas").principal(ESTUDIANTE).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.turnoId").value("El turno es obligatorio"));

        verifyNoInteractions(reservaService);
    }

    @Test
    void cancelarDevuelve200ConQuienCancelo() throws Exception {
        when(reservaService.cancelar(eq(7L), any())).thenReturn(reserva(EstadoReserva.CANCELADA, Rol.ESTUDIANTE));

        mockMvc.perform(patch("/api/reservas/7/cancelacion").principal(ESTUDIANTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.canceladaPor").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.fechaCancelacion").exists());
    }

    @Test
    void cancelarReservaYaCanceladaDevuelve409() throws Exception {
        String mensaje = "Solo se pueden cancelar reservas activas.";
        when(reservaService.cancelar(eq(7L), any())).thenThrow(new ConflictoException(mensaje));

        mockMvc.perform(patch("/api/reservas/7/cancelacion").principal(ESTUDIANTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void cancelarTurnoQueYaEmpezoDevuelve409() throws Exception {
        String mensaje = "No se puede cancelar la reserva de un turno que ya empezó.";
        when(reservaService.cancelar(eq(7L), any())).thenThrow(new ConflictoException(mensaje));

        mockMvc.perform(patch("/api/reservas/7/cancelacion").principal(ESTUDIANTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void cancelarReservaDeOtroUsuarioDevuelve403() throws Exception {
        String mensaje = "Solo pueden cancelar la reserva el estudiante que reservó, el tutor del turno o un administrador.";
        when(reservaService.cancelar(eq(7L), any())).thenThrow(new AccesoDenegadoException(mensaje));

        mockMvc.perform(patch("/api/reservas/7/cancelacion").principal(ESTUDIANTE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }

    @Test
    void listarActivasFuturasDevuelve200() throws Exception {
        when(reservaService.listarActivasFuturas()).thenReturn(List.of(reserva(EstadoReserva.ACTIVA, null)));

        mockMvc.perform(get("/api/reservas/activas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].estudiante").value("Ana Pérez"));
    }

    @Test
    void listarProximasDeEstudianteDevuelveElLinkDeLaReunion() throws Exception {
        when(reservaService.listarProximasDeEstudiante("juan@mail.com")).thenReturn(List.of(reserva(EstadoReserva.ACTIVA, null)));

        mockMvc.perform(get("/api/reservas/mis-reservas").principal(ESTUDIANTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tutor").value("Juan Gómez"))
                .andExpect(jsonPath("$[0].link").value("https://meet.google.com/abc"));
    }

    @Test
    void laReservaSeHaceANombreDelEstudianteDeLaSesion() throws Exception {
        when(reservaService.reservar(any(), any())).thenReturn(reserva(EstadoReserva.ACTIVA, null));

        mockMvc.perform(post("/api/reservas").principal(ESTUDIANTE).contentType(MediaType.APPLICATION_JSON).content(RESERVA))
                .andExpect(status().isCreated());

        verify(reservaService).reservar(any(), eq("juan@mail.com"));
    }
}
