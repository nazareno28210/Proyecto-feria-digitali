package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Stand;
import jakarta.persistence.*;

import java.util.List;
import java.util.stream.Collectors;

public class StandDTO {
    private int id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private List<ProductoDTO> productos;
    private FerianteDTO feriante;


    public StandDTO(Stand stand) {
        this.id=stand.getId();
        this.nombre =stand.getNombre();
        this.descripcion = stand.getDescripcion();
        this.imagenUrl = stand.getImagenUrl();
        this.productos =stand.getProductos()
                .stream()
                .map(ProductoDTO::new)
                .collect(Collectors.toList());
        this.feriante=getFeriante();
        if (stand.getFeriante() != null) {
            this.feriante = new FerianteDTO(stand.getFeriante());
        }
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<ProductoDTO> getProductos() {
        return productos;
    }

    public FerianteDTO getFeriante() {
        return feriante;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

}
