package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feria;

import java.time.LocalDate;

public class FeriaDTO {
    private int id;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private String Lugar;
    private String descripcion;
    private String estado;

    public FeriaDTO(){}
    public FeriaDTO(Feria feria) {
        this.nombre = feria.getNombre();
        this.fechaInicio = feria.getFechaInicio();
        this.fechaFinal = feria.getFechaFinal();
        this.Lugar = feria.getLugar();
        this.descripcion = feria.getDescripcion();
        this.estado = feria.getEstado();

    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFinal() {
        return fechaFinal;
    }

    public String getLugar() {
        return Lugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }
}
