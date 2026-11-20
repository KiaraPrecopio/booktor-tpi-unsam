package com.mdd.booktor.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Materia;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Reserva;
import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Turno;
import com.mdd.booktor.models.Usuario;
import com.mdd.booktor.repositories.ReservaRepository;
import com.mdd.booktor.repositories.TurnoRepository;
import com.mdd.booktor.repositories.UsuarioRepository;
import com.mdd.booktor.services.impl.ReservaServiceImpl;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ReservaStepDefinitions {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 20, 10, 0);
    private static final String EMAIL = "estudiante@booktor.com";
    private static final Long TURNO_ID = 1L;

    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private final TurnoRepository turnoRepository = mock(TurnoRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final ReservaServiceImpl service = new ReservaServiceImpl(reservaRepository, turnoRepository,
            usuarioRepository, Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA));

    private final Usuario estudiante = new Usuario("Juan", "Pérez", EMAIL, "hash", Rol.ESTUDIANTE, true);
    private final Usuario tutor = new Usuario("Lucía", "Fernández", "tutor@booktor.com", "hash", Rol.TUTOR, true);
    private final Materia materia = new Materia("Programación III", null, true);

    private Turno turno;
    private Reserva reservaGuardada;
    private ReservaResponse respuesta;
    private ConflictoException error;

    public ReservaStepDefinitions() {
        when(usuarioRepository.findByEmailIgnoreCaseAndRolAndActivoTrue(EMAIL, Rol.ESTUDIANTE))
                .thenReturn(Optional.of(estudiante));
        // Guardar devuelve la misma reserva, y la recordamos para revisarla en los pasos Then.
        when(reservaRepository.saveAndFlush(any(Reserva.class))).thenAnswer(invocacion -> {
            reservaGuardada = invocacion.getArgument(0);
            return reservaGuardada;
        });
    }

    // =========================================================
    // GIVEN
    // =========================================================

    @Given("un turno disponible que empieza en {int} minutos")
    public void unTurnoDisponibleQueEmpiezaEn(int minutos) {
        turno = turnoQueEmpiezaEn(minutos);
    }

    @Given("un turno asignado que empieza en {int} minutos")
    public void unTurnoAsignadoQueEmpiezaEn(int minutos) {
        turno = turnoQueEmpiezaEn(minutos);
        turno.setEstado(EstadoTurno.ASIGNADO);
    }

    @Given("el estudiante ya tiene otra reserva activa en ese horario")
    public void elEstudianteYaTieneOtraReservaActivaEnEseHorario() {
        Turno otroTurno = new Turno(tutor, materia, turno.getInicio(), turno.getFin(), Modalidad.VIRTUAL, "https://meet.com/otro", null);
        Reserva otraReserva = new Reserva(otroTurno, estudiante, AHORA, null);
        when(reservaRepository.buscarActivasSuperpuestas(estudiante.getId(), turno.getInicio(), turno.getFin()))
                .thenReturn(List.of(otraReserva));
    }

    // =========================================================
    // WHEN
    // =========================================================

    @When("el estudiante reserva el turno con el objetivo {string}")
    public void elEstudianteReservaElTurnoConElObjetivo(String objetivo) {
        reservar(objetivo);
    }

    @When("el estudiante intenta reservar el turno")
    public void elEstudianteIntentaReservarElTurno() {
        reservar(null);
    }

    // =========================================================
    // THEN
    // =========================================================

    @Then("la reserva queda activa con el objetivo {string}")
    public void laReservaQuedaActivaConElObjetivo(String objetivo) {
        assertNull(error);
        assertEquals(EstadoReserva.ACTIVA, reservaGuardada.getEstado());
        assertEquals(objetivo, respuesta.objetivo());
    }

    @Then("el turno queda asignado")
    public void elTurnoQuedaAsignado() {
        assertEquals(EstadoTurno.ASIGNADO, turno.getEstado());
    }

    @Then("el turno sigue disponible")
    public void elTurnoSigueDisponible() {
        assertEquals(EstadoTurno.DISPONIBLE, turno.getEstado());
    }

    @Then("la reserva es aceptada")
    public void laReservaEsAceptada() {
        assertNull(error);
        assertNotNull(respuesta);
    }

    @Then("la reserva es rechazada")
    public void laReservaEsRechazada() {
        assertNotNull(error);
        assertNull(reservaGuardada);
    }

    @Then("la reserva es rechazada con el mensaje {string}")
    public void laReservaEsRechazadaConElMensaje(String mensaje) {
        laReservaEsRechazada();
        assertEquals(mensaje, error.getMessage());
    }

    // =========================================================
    // AUXILIARES
    // =========================================================

    private Turno turnoQueEmpiezaEn(int minutos) {
        LocalDateTime inicio = AHORA.plusMinutes(minutos);
        Turno nuevo = new Turno(tutor, materia, inicio, inicio.plusMinutes(60), Modalidad.VIRTUAL, "https://meet.com/booktor", null);
        when(turnoRepository.findById(TURNO_ID)).thenReturn(Optional.of(nuevo));
        return nuevo;
    }

    // Si el service rechaza la reserva, guardamos el error para revisarlo en los pasos Then.
    private void reservar(String objetivo) {
        try {
            respuesta = service.reservar(new ReservaRequest(TURNO_ID, objetivo), EMAIL);
        } catch (ConflictoException e) {
            error = e;
        }
    }
}
