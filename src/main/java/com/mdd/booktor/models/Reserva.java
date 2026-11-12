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

    public Reserva() {
    }

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
