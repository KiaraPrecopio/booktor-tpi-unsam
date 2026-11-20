package com.mdd.booktor.models;

import jakarta.persistence.*;

/** Materia en la que se ofrecen tutorías. La baja es lógica ({@code activo = false}). */
@Entity
public class Materia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    private String descripcion;

    private Boolean activo;

    /** Constructor vacío que necesita JPA para crear la entidad al leerla de la base. */
    public Materia() {

    }

    /**
     * Crea una materia.
     *
     * @param nombre nombre único entre las activas
     * @param descripcion descripción opcional
     * @param activo si está activa
     */
    public Materia(String nombre, String descripcion, Boolean activo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
