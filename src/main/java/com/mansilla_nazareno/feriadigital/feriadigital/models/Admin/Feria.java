package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ferias")
public class Feria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private String lugar;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String imagenUrl;
    private Double latitud;
    private Double longitud;
    private Integer capacidad;

    @Column(nullable = false)
    private boolean eliminado = false;

    // Constructor vacío obligatorio para JPA
    public Feria() {}

    // Constructor actualizado (sin campos temporales)
    public Feria(String nombre, String lugar, String descripcion, String imagenUrl, Double latitud, Double longitud, Integer capacidad) {
        this.nombre = nombre;
        this.lugar = lugar;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.latitud = latitud;
        this.longitud = longitud;
        this.capacidad = capacidad;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
}

