package com.mdd.booktor.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Apoyo de accesibilidad que puede ofrecer un tutor, por ejemplo material visual o intérprete de lengua de señas.
 * El estudiante filtra tutores por las adaptaciones que necesita. La baja es lógica ({@code activo = false}).
 */
@Entity
public class Adaptacion {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo;

    /** Constructor vacío que necesita JPA para crear la entidad al leerla de la base. */
    public Adaptacion() {
    }
    
    /**
     * Crea una adaptación.
     *
     * @param nombre nombre único entre las activas
     * @param descripcion descripción opcional
     * @param activo si está activa
     */
    public Adaptacion(String nombre, String descripcion, boolean activo) {
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

    public boolean isActivo() {
        return activo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
