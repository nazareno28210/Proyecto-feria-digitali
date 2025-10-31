package com.mansilla_nazareno.feriadigital.feriadigital.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Feria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private String lugar;
    private String descripcion;
    private String estado;

    @OneToMany(mappedBy = "feria", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("feria") // Rompe la recursividad
    private List<Stand> stands;

    public Feria(){}
    public Feria(String nombre, LocalDate fechaInicio, LocalDate fechaFinal, String lugar, String descripcion, String estado,String Lugar) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
        this.lugar = lugar;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        lugar = lugar;
    }

    public void setStands(List<Stand> stands) {
        this.stands = stands;
    }

    public LocalDate getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(LocalDate fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Stand> getStands() {
        return stands;
    }
}
