package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Stand;
import jakarta.persistence.*;

import java.util.List;

public class StandDTO {
    private int id;
    private String nombre;
    private String descripcion;
    private Feria feria;
    private List<Producto> productos;

    public StandDTO(Stand stand) {
        this.id=stand.getId();
        this.nombre =stand.getNombre();
        this.descripcion = stand.getDescripcion();
        this.productos =stand.getProductos();
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

    public List<Producto> getProductos() {
        return productos;
    }
}
