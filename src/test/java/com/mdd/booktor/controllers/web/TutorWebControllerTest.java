package com.mdd.booktor.controllers.web;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mdd.booktor.configs.JwtConfig;
import com.mdd.booktor.configs.SecurityConfig;
import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.dtos.TutorRequest;
import com.mdd.booktor.dtos.TutorResponse;
import com.mdd.booktor.services.AdaptacionService;
import com.mdd.booktor.services.TutorService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TutorWebController.class)
@Import({ SecurityConfig.class, JwtConfig.class })
@WithMockUser(roles = "ADMINISTRADOR")
class TutorWebControllerTest {

    private static final AdaptacionResponse DISLEXIA = new AdaptacionResponse(3L, "Dislexia", null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TutorService tutorService;
    @MockitoBean
    private AdaptacionService adaptacionService;

    private TutorResponse ana() {
        return new TutorResponse(5L, "Ana", "Gómez", "ana@mail.com", 45, List.of(DISLEXIA));
    }

    @Test
    void listaLosTutoresConSuDuracion() throws Exception {
        when(tutorService.listar()).thenReturn(List.of(ana()));

        mockMvc.perform(get("/admin/tutores"))
                .andExpect(content().string(containsString("Ana Gómez")))
                .andExpect(content().string(containsString("45 min")));
    }

    @Test
    void crearArmaElPedidoConDuracionYAdaptaciones() throws Exception {
        mockMvc.perform(post("/admin/tutores").param("nombre", "Ana").param("apellido", "Gómez")
                        .param("email", "ana@mail.com").param("password", "Prueba1234")
                        .param("duracionTurnoMinutos", "45").param("adaptacionIds", "3"))
                .andExpect(redirectedUrl("/admin/tutores"));

        verify(tutorService).crear(new TutorRequest("Ana", "Gómez", "ana@mail.com", "Prueba1234", 45, Set.of(3L)));
    }

    @Test
    void unTutorSinAdaptacionesMuestraElError() throws Exception {
        mockMvc.perform(post("/admin/tutores").param("nombre", "Ana").param("apellido", "Gómez")
                        .param("email", "ana@mail.com").param("password", "Prueba1234").param("duracionTurnoMinutos", "45"))
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("El tutor tiene que ofrecer al menos una adaptación")));

        verifyNoInteractions(tutorService);
    }

    @Test
    void editarMuestraLaDuracionYLasAdaptacionesQueOfrece() throws Exception {
        when(tutorService.buscarPorId(5L)).thenReturn(ana());
        when(adaptacionService.listar()).thenReturn(List.of(DISLEXIA));

        mockMvc.perform(get("/admin/tutores/editar").param("id", "5"))
                .andExpect(content().string(containsString("value=\"45\"")))
                .andExpect(content().string(containsString("value=\"3\" checked")));
    }

    @Test
    void modificarVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/tutores/modificar").param("id", "5").param("nombre", "Ana")
                        .param("apellido", "Gómez").param("email", "ana@mail.com").param("password", "")
                        .param("duracionTurnoMinutos", "60").param("adaptacionIds", "3"))
                .andExpect(redirectedUrl("/admin/tutores"));

        verify(tutorService).modificar(5L, new TutorRequest("Ana", "Gómez", "ana@mail.com", "", 60, Set.of(3L)));
    }

    @Test
    void darDeBajaVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/tutores/baja").param("id", "5"))
                .andExpect(redirectedUrl("/admin/tutores"));

        verify(tutorService).darDeBaja(5L);
    }
}
