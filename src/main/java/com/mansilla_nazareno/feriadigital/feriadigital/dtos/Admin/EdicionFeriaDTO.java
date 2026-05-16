package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;

import java.time.LocalDate;

public class EdicionFeriaDTO {

    private Integer id;
    private Integer feriaId;
    private String nombreFeriaBase;
    private String nombreEdicion;
    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private String estado;

    public EdicionFeriaDTO() {}

    public EdicionFeriaDTO(EdicionFeria edicion) {
        this.id = edicion.getId();
        this.nombreEdicion = edicion.getNombreEdicion();
        this.fechaInicio = edicion.getFechaInicio();
        this.fechaFinal = edicion.getFechaFinal();
        this.estado = edicion.getEstado();
        if (edicion.getFeria() != null) {
            this.feriaId = edicion.getFeria().getId();
            this.nombreFeriaBase = edicion.getFeria().getNombre();
        }
    }

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFeriaId() {
        return feriaId;
    }

    public void setFeriaId(Integer feriaId) {
        this.feriaId = feriaId;
    }

    public String getNombreFeriaBase() {
        return nombreFeriaBase;
    }

    public void setNombreFeriaBase(String nombreFeriaBase) {
        this.nombreFeriaBase = nombreFeriaBase;
    }

    public String getNombreEdicion() {
        return nombreEdicion;
    }

    public void setNombreEdicion(String nombreEdicion) {
        this.nombreEdicion = nombreEdicion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(LocalDate fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
