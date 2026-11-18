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
import com.mdd.booktor.dtos.EstudianteRequest;
import com.mdd.booktor.dtos.EstudianteResponse;
import com.mdd.booktor.services.AdaptacionService;
import com.mdd.booktor.services.EstudianteService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(EstudianteWebController.class)
@Import({ SecurityConfig.class, JwtConfig.class })
@WithMockUser(roles = "ADMINISTRADOR")
class EstudianteWebControllerTest {

    private static final AdaptacionResponse DISLEXIA = new AdaptacionResponse(3L, "Dislexia", null);
    private static final AdaptacionResponse TDAH = new AdaptacionResponse(4L, "TDAH", null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstudianteService estudianteService;
    @MockitoBean
    private AdaptacionService adaptacionService;

    private EstudianteResponse juan() {
        return new EstudianteResponse(1L, "Juan", "Pérez", "juan@mail.com", List.of(DISLEXIA));
    }

    @Test
    void listaLosEstudiantesConSusAdaptaciones() throws Exception {
        when(estudianteService.listar()).thenReturn(List.of(juan()));
        when(adaptacionService.listar()).thenReturn(List.of(DISLEXIA, TDAH));

        mockMvc.perform(get("/admin/estudiantes"))
                .andExpect(content().string(containsString("juan@mail.com")))
                .andExpect(content().string(containsString("Dislexia.")))
                .andExpect(content().string(containsString("name=\"adaptacionIds\" value=\"4\"")));
    }

    @Test
    void crearArmaElPedidoConLasAdaptacionesMarcadas() throws Exception {
        mockMvc.perform(post("/admin/estudiantes").param("nombre", "Juan").param("apellido", "Pérez")
                        .param("email", "juan@mail.com").param("password", "Prueba1234")
                        .param("adaptacionIds", "3").param("adaptacionIds", "4"))
                .andExpect(redirectedUrl("/admin/estudiantes"));

        verify(estudianteService).crear(new EstudianteRequest("Juan", "Pérez", "juan@mail.com", "Prueba1234", Set.of(3L, 4L)));
    }

    @Test
    void editarDejaMarcadasLasAdaptacionesQueTiene() throws Exception {
        when(estudianteService.buscarPorId(1L)).thenReturn(juan());
        when(adaptacionService.listar()).thenReturn(List.of(DISLEXIA, TDAH));

        mockMvc.perform(get("/admin/estudiantes/editar").param("id", "1"))
                .andExpect(content().string(containsString("value=\"3\" checked")))
                .andExpect(content().string(containsString("value=\"4\">")));
    }

    @Test
    void modificarSinContrasenaNoLaCambia() throws Exception {
        mockMvc.perform(post("/admin/estudiantes/modificar").param("id", "1").param("nombre", "Juan")
                        .param("apellido", "Pérez").param("email", "juan@mail.com").param("password", ""))
                .andExpect(redirectedUrl("/admin/estudiantes"));

        verify(estudianteService).modificar(1L, new EstudianteRequest("Juan", "Pérez", "juan@mail.com", "", null));
    }

    @Test
    void darDeBajaVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/estudiantes/baja").param("id", "1"))
                .andExpect(redirectedUrl("/admin/estudiantes"));

        verify(estudianteService).darDeBaja(1L);
    }
}
