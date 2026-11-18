package com.mdd.booktor.controllers.web;

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
import com.mdd.booktor.dtos.AdaptacionRequest;
import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.services.AdaptacionService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(AdaptacionWebController.class)
@Import({ SecurityConfig.class, JwtConfig.class })
@WithMockUser(roles = "ADMINISTRADOR")
class AdaptacionWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdaptacionService adaptacionService;

    @Test
    void listaLasAdaptaciones() throws Exception {
        when(adaptacionService.listar()).thenReturn(List.of(new AdaptacionResponse(3L, "Dislexia", "Texto ampliado")));

        mockMvc.perform(get("/admin/adaptaciones"))
                .andExpect(content().string(containsString("Dislexia")));
    }

    @Test
    void crearVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/adaptaciones").param("nombre", "TDAH").param("descripcion", "Pausas"))
                .andExpect(redirectedUrl("/admin/adaptaciones"));

        verify(adaptacionService).crear(new AdaptacionRequest("TDAH", "Pausas"));
    }

    @Test
    void editarMuestraLosDatosActuales() throws Exception {
        when(adaptacionService.buscarPorId(3L)).thenReturn(new AdaptacionResponse(3L, "Dislexia", "Texto ampliado"));

        mockMvc.perform(get("/admin/adaptaciones/editar").param("id", "3"))
                .andExpect(content().string(containsString("value=\"Texto ampliado\"")));
    }

    @Test
    void modificarVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/adaptaciones/modificar").param("id", "3").param("nombre", "Dislexia"))
                .andExpect(redirectedUrl("/admin/adaptaciones"));

        verify(adaptacionService).modificar(3L, new AdaptacionRequest("Dislexia", null));
    }

    @Test
    void darDeBajaVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/adaptaciones/baja").param("id", "3"))
                .andExpect(redirectedUrl("/admin/adaptaciones"));

        verify(adaptacionService).darDeBaja(3L);
    }
}
