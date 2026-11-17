package com.mdd.booktor.configs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mdd.booktor.controllers.BusquedaController;
import com.mdd.booktor.controllers.InicioController;
import com.mdd.booktor.controllers.MateriaController;
import com.mdd.booktor.controllers.ReservaController;
import com.mdd.booktor.controllers.TurnoController;
import com.mdd.booktor.controllers.TutorController;
import com.mdd.booktor.controllers.web.PanelAdminController;
import com.mdd.booktor.controllers.web.PanelEstudianteController;
import com.mdd.booktor.controllers.web.PanelTutorController;
import com.mdd.booktor.services.AdaptacionService;
import com.mdd.booktor.services.BusquedaService;
import com.mdd.booktor.services.MateriaService;
import com.mdd.booktor.services.ReservaService;
import com.mdd.booktor.services.TurnoService;
import com.mdd.booktor.services.TutorService;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ MateriaController.class, TutorController.class, TurnoController.class,
        BusquedaController.class, ReservaController.class, InicioController.class,
        PanelAdminController.class, PanelTutorController.class, PanelEstudianteController.class })
@Import(SecurityConfig.class)
class SeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MateriaService materiaService;
    @MockitoBean
    private TutorService tutorService;
    @MockitoBean
    private TurnoService turnoService;
    @MockitoBean
    private BusquedaService busquedaService;
    @MockitoBean
    private ReservaService reservaService;
    @MockitoBean
    private AdaptacionService adaptacionService;

    @Test
    void sinSesionLaApiDevuelve401() throws Exception {
        mockMvc.perform(get("/api/materias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinSesionElMenuRedirigeAlLogin() throws Exception {
        mockMvc.perform(get("/").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void sinSesionSePuedeVerElLoginConElTokenCsrf() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void alIniciarSesionElAdministradorVaASuMenu() throws Exception {
        mockMvc.perform(get("/")).andExpect(redirectedUrl("/admin"));
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    void alIniciarSesionElTutorVaASuMenu() throws Exception {
        mockMvc.perform(get("/")).andExpect(redirectedUrl("/tutor"));
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void alIniciarSesionElEstudianteVaASuMenu() throws Exception {
        mockMvc.perform(get("/")).andExpect(redirectedUrl("/estudiante"));
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void elEstudianteVeSuMenuConElTokenParaCerrarSesion() throws Exception {
        mockMvc.perform(get("/estudiante"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void elEstudianteNoEntraALasPantallasDelAdministrador() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    void elTutorNoEntraALasPantallasDelEstudiante() throws Exception {
        mockMvc.perform(get("/estudiante")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void elAdministradorNoEntraALasPantallasDelTutor() throws Exception {
        mockMvc.perform(get("/tutor")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorAccedeAlAbmlDeTutores() throws Exception {
        mockMvc.perform(get("/api/tutores")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorVeLasReservasActivas() throws Exception {
        mockMvc.perform(get("/api/reservas/activas")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorNoGeneraTurnos() throws Exception {
        mockMvc.perform(get("/api/turnos/mis-turnos")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    void tutorVeSusTurnos() throws Exception {
        mockMvc.perform(get("/api/turnos/mis-turnos")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    void tutorConsultaLasMaterias() throws Exception {
        mockMvc.perform(get("/api/materias")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    void tutorNoAccedeAlAbmlDeTutores() throws Exception {
        mockMvc.perform(get("/api/tutores")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    void tutorNoBuscaTurnosParaReservar() throws Exception {
        mockMvc.perform(get("/api/busqueda/materias")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    void tutorPuedeCancelarUnaReserva() throws Exception {
        mockMvc.perform(patch("/api/reservas/7/cancelacion"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void estudianteBuscaMaterias() throws Exception {
        mockMvc.perform(get("/api/busqueda/materias")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void estudianteVeSusReservas() throws Exception {
        mockMvc.perform(get("/api/reservas/mis-reservas")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void estudianteNoModificaMaterias() throws Exception {
        mockMvc.perform(post("/api/materias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Física\", \"descripcion\": \"Mecánica\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void estudianteNoGeneraTurnos() throws Exception {
        mockMvc.perform(post("/api/turnos/bloques").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ESTUDIANTE")
    void estudianteNoVeTodasLasReservasActivas() throws Exception {
        mockMvc.perform(get("/api/reservas/activas")).andExpect(status().isForbidden());
    }
}
