package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Stand;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class FeriaDTO {
    private int id;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private String lugar;
    private String descripcion;
    private String estado;
    private String imagenUrl;

    private List<StandDTO> stands;
    public FeriaDTO(){}
    public FeriaDTO(Feria feria) {
        this.id =feria.getId();
        this.nombre = feria.getNombre();
        this.fechaInicio = feria.getFechaInicio();
        this.fechaFinal = feria.getFechaFinal();
        this.lugar = feria.getLugar();
        this.descripcion = feria.getDescripcion();
        this.estado = feria.getEstado();
        this.imagenUrl = feria.getImagenUrl();
        this.stands =feria.getStands()
                .stream()
                .map(StandDTO::new)
                .collect(Collectors.toList());

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
        return lugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }
    public List<StandDTO> getStands() {
        return stands;
    }
}
