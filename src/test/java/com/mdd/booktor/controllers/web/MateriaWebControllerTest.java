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
import com.mdd.booktor.dtos.MateriaRequest;
import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.services.MateriaService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MateriaWebController.class)
@Import({ SecurityConfig.class, JwtConfig.class })
@WithMockUser(roles = "ADMINISTRADOR")
class MateriaWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MateriaService materiaService;

    @Test
    void listaLasMaterias() throws Exception {
        when(materiaService.listar()).thenReturn(List.of(new MateriaResponse(2L, "Análisis I", "Límites")));

        mockMvc.perform(get("/admin/materias"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Análisis I")))
                .andExpect(content().string(containsString("name=\"id\" value=\"2\"")));
    }

    @Test
    void crearVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/materias").param("nombre", "Física").param("descripcion", ""))
                .andExpect(redirectedUrl("/admin/materias"));

        verify(materiaService).crear(new MateriaRequest("Física", ""));
    }

    @Test
    void crearSinNombreMuestraElError() throws Exception {
        mockMvc.perform(post("/admin/materias").param("nombre", " "))
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("El nombre es obligatorio")));

        verifyNoInteractions(materiaService);
    }

    @Test
    void editarMuestraLosDatosActuales() throws Exception {
        when(materiaService.buscarPorId(2L)).thenReturn(new MateriaResponse(2L, "Análisis I", "Límites"));

        mockMvc.perform(get("/admin/materias/editar").param("id", "2"))
                .andExpect(content().string(containsString("value=\"Análisis I\"")));
    }

    @Test
    void modificarVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/materias/modificar").param("id", "2")
                        .param("nombre", "Análisis II").param("descripcion", "Integrales"))
                .andExpect(redirectedUrl("/admin/materias"));

        verify(materiaService).modificar(2L, new MateriaRequest("Análisis II", "Integrales"));
    }

    @Test
    void siNoSePuedeDarDeBajaSeMuestraElMotivo() throws Exception {
        doThrow(new ConflictoException("La materia tiene turnos futuros.")).when(materiaService).darDeBaja(2L);

        mockMvc.perform(post("/admin/materias/baja").param("id", "2"))
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("La materia tiene turnos futuros.")));
    }

    @Test
    void darDeBajaVuelveALaLista() throws Exception {
        mockMvc.perform(post("/admin/materias/baja").param("id", "2"))
                .andExpect(redirectedUrl("/admin/materias"));

        verify(materiaService).darDeBaja(2L);
    }
}
