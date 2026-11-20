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
 * Horario que publica un tutor para una materia, virtual (con link) o presencial (con lugar).
 * Se crea al generar un bloque y su estado cambia según {@link EstadoTurno}.
 */
@Entity
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id")
    private Usuario tutor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id")
    private Materia materia;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTurno estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidad modalidad;

    private String link;

    private String lugar;

    /** Constructor vacío que necesita JPA para crear la entidad al leerla de la base. */
    public Turno() {
    }

    /**
     * Crea un turno en estado DISPONIBLE.
     *
     * @param tutor tutor que lo ofrece
     * @param materia materia del turno
     * @param inicio fecha y hora de inicio
     * @param fin fecha y hora de fin
     * @param modalidad virtual o presencial
     * @param link link de la reunión, para los virtuales
     * @param lugar lugar del encuentro, para los presenciales
     */
    public Turno(Usuario tutor, Materia materia, LocalDateTime inicio, LocalDateTime fin, Modalidad modalidad, String link, String lugar) {
        this.tutor = tutor;
        this.materia = materia;
        this.inicio = inicio;
        this.fin = fin;
        this.estado = EstadoTurno.DISPONIBLE;
        this.modalidad = modalidad;
        this.link = link;
        this.lugar = lugar;
    }

    public Long getId() {
        return id;
    }

    public Usuario getTutor() {
        return tutor;
    }

    public Materia getMateria() {
        return materia;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public String getLink() {
        return link;
    }

    public String getLugar() {
        return lugar;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }
}
