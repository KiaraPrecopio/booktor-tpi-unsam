package com.mdd.booktor.controllers.web;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mdd.booktor.configs.JwtConfig;
import com.mdd.booktor.configs.SecurityConfig;
import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.services.ReservaService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PanelAdminController.class)
@Import({ SecurityConfig.class, JwtConfig.class })
@WithMockUser(username = "admin@mail.com", roles = "ADMINISTRADOR")
class PanelAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService reservaService;

    @Test
    void elMenuTieneLosLinksDelAdministrador() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/admin/tutores")))
                .andExpect(content().string(containsString("/admin/reservas")));
    }

    @Test
    void muestraLasReservasActivas() throws Exception {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 20, 14, 0);
        when(reservaService.listarActivasFuturas()).thenReturn(List.of(new ReservaResponse(7L, 10L, 1L, "Juan Pérez",
                "Análisis I", "Ana Gómez", inicio, inicio.plusHours(1), Modalidad.VIRTUAL, "https://meet/abc", null,
                EstadoReserva.ACTIVA, inicio.minusDays(1), null, null, null)));

        mockMvc.perform(get("/admin/reservas"))
                .andExpect(content().string(containsString("Juan Pérez")))
                .andExpect(content().string(containsString("20/11/2026 14:00")));
    }

    @Test
    void cancelarUnaReservaVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/reservas/cancelar").param("reservaId", "7"))
                .andExpect(redirectedUrl("/admin/reservas"));

        verify(reservaService).cancelar(7L, "admin@mail.com");
    }
}
