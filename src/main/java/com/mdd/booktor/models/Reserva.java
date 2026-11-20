package com.mdd.booktor.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Reserva de un turno por parte de un estudiante, con el objetivo que quiere trabajar.
 * Si se cancela, guarda quién la canceló y cuándo. Su estado cambia según {@link EstadoReserva}.
 */
@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turno_id")
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id")
    private Usuario estudiante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(nullable = false)
    private LocalDateTime fechaReserva;

    @Column(length = 500)
    private String objetivo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Rol canceladaPor;

    private LocalDateTime fechaCancelacion;

    /** Constructor vacío que necesita JPA para crear la entidad al leerla de la base. */
    public Reserva() {
    }

    /**
     * Crea una reserva en estado ACTIVA.
     *
     * @param turno turno reservado
     * @param estudiante estudiante que reserva
     * @param fechaReserva momento en que se hizo la reserva
     * @param objetivo qué quiere trabajar el estudiante, opcional
     */
    public Reserva(Turno turno, Usuario estudiante, LocalDateTime fechaReserva, String objetivo) {
        this.turno = turno;
        this.estudiante = estudiante;
        this.estado = EstadoReserva.ACTIVA;
        this.fechaReserva = fechaReserva;
        this.objetivo = objetivo;
    }

    public Long getId() {
        return id;
    }

    public Turno getTurno() {
        return turno;
    }

    public Usuario getEstudiante() {
        return estudiante;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public Rol getCanceladaPor() {
        return canceladaPor;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public void setCanceladaPor(Rol canceladaPor) {
        this.canceladaPor = canceladaPor;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }
}
