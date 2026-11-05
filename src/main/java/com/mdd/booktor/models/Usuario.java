package com.mdd.booktor.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    private Integer duracionTurnoMinutos;

    @Column(nullable = false)
    private boolean activo;

    @ManyToMany
    @JoinTable(
        name = "usuario_adaptacion",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "adaptacion_id")
    )
    private Set<Adaptacion> adaptaciones = new HashSet<>();

    public Usuario() {
    }

    public Usuario(String nombre, String apellido, String email, String password, Rol rol, boolean activo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Rol getRol() {
        return rol;
    }

    public Integer getDuracionTurnoMinutos() {
        return duracionTurnoMinutos;
    }

    public boolean isActivo() {
        return activo;
    }

    public Set<Adaptacion> getAdaptaciones() {
        return adaptaciones;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setDuracionTurnoMinutos(Integer duracionTurnoMinutos) {
        this.duracionTurnoMinutos = duracionTurnoMinutos;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setAdaptaciones(Set<Adaptacion> adaptaciones) {
        this.adaptaciones = adaptaciones;
    }
}
