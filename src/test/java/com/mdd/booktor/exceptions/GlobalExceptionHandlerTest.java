package com.mdd.booktor.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ControllerDePrueba.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void recursoNoEncontradoDevuelve404() throws Exception {
        mockMvc.perform(get("/prueba/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la materia con id 99"));
    }

    @Test
    void conflictoDevuelve409() throws Exception {
        mockMvc.perform(get("/prueba/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No se puede dar de baja la materia: tiene turnos futuros"));
    }

    @Test
    void datosInvalidosDevuelve400ConTodosLosCampos() throws Exception {
        mockMvc.perform(post("/prueba/validacion")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"))
               .andExpect(jsonPath("$.errores.email").value("El email es obligatorio"));
    }
}