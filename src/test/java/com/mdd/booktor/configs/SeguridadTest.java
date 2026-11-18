package com.mdd.booktor.configs;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mdd.booktor.controllers.AuthController;
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
import com.mdd.booktor.services.JwtService;
import com.mdd.booktor.services.MateriaService;
import com.mdd.booktor.services.ReservaService;
import com.mdd.booktor.services.TurnoService;
import com.mdd.booktor.services.TutorService;

import jakarta.servlet.http.Cookie;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ MateriaController.class, TutorController.class, TurnoController.class,
        BusquedaController.class, ReservaController.class, InicioController.class, AuthController.class,
        PanelAdminController.class, PanelTutorController.class, PanelEstudianteController.class })
@Import({ SecurityConfig.class, JwtConfig.class, JwtService.class, RelojConfig.class, PasswordEncoderConfig.class })
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
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private void existeElTutorAna() {
        when(userDetailsService.loadUserByUsername("ana@mail.com")).thenReturn(User.withUsername("ana@mail.com")
                .password(passwordEncoder.encode("Prueba1234")).roles("TUTOR").build());
    }

    private String tokenDeTutor() {
        return jwtService.generarToken(new UsernamePasswordAuthenticationToken("ana@mail.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_TUTOR"))));
    }

    @Test
    void elLoginDeLaApiDevuelveUnToken() throws Exception {
        existeElTutorAna();

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ana@mail.com\", \"password\": \"Prueba1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void elLoginDeLaApiConContraseniaIncorrectaDevuelve401() throws Exception {
        existeElTutorAna();

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ana@mail.com\", \"password\": \"otra\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conElTokenEnElEncabezadoSeUsaLaApiDelRol() throws Exception {
        mockMvc.perform(get("/api/turnos/mis-turnos").header("Authorization", "Bearer " + tokenDeTutor()))
                .andExpect(status().isOk());
    }

    @Test
    void elTokenDeTutorNoSirveParaLoDelAdministrador() throws Exception {
        mockMvc.perform(get("/api/tutores").header("Authorization", "Bearer " + tokenDeTutor()))
                .andExpect(status().isForbidden());
    }

    @Test
    void conUnTokenInvalidoLaApiDevuelve401() throws Exception {
        mockMvc.perform(get("/api/turnos/mis-turnos").header("Authorization", "Bearer inventado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elLoginWebGuardaElTokenEnUnaCookieYVaAlMenu() throws Exception {
        existeElTutorAna();

        mockMvc.perform(post("/login").param("email", "ana@mail.com").param("password", "Prueba1234"))
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().exists(JwtService.COOKIE))
                .andExpect(cookie().httpOnly(JwtService.COOKIE, true))
                .andExpect(cookie().sameSite(JwtService.COOKIE, "Strict"));
    }

    @Test
    void elLoginWebConContraseniaIncorrectaVuelveAlLoginConError() throws Exception {
        existeElTutorAna();

        mockMvc.perform(post("/login").param("email", "ana@mail.com").param("password", "otra"))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(cookie().doesNotExist(JwtService.COOKIE));
    }

    @Test
    void conLaCookieSeVenLasPantallasDelRol() throws Exception {
        mockMvc.perform(get("/tutor").cookie(new Cookie(JwtService.COOKIE, tokenDeTutor())))
                .andExpect(status().isOk());
    }

    @Test
    void conUnaCookieInvalidaSeBorraYVuelveAlLogin() throws Exception {
        mockMvc.perform(get("/tutor").cookie(new Cookie(JwtService.COOKIE, "inventado")))
                .andExpect(redirectedUrl("/login"))
                .andExpect(cookie().maxAge(JwtService.COOKIE, 0));
    }

    @Test
    void cerrarSesionBorraLaCookie() throws Exception {
        mockMvc.perform(post("/logout").cookie(new Cookie(JwtService.COOKIE, tokenDeTutor())))
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge(JwtService.COOKIE, 0));
    }

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
    void sinSesionSePuedeVerElLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"email\"")));
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
    void elEstudianteVeSuMenuConElBotonParaCerrarSesion() throws Exception {
        mockMvc.perform(get("/estudiante"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/logout\"")));
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
