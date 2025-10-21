package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Stand;
import jakarta.persistence.*;

import java.util.List;

public class ProductoDTO {
    private int id;
    private String nombre;
    private String Descripcion;
    private double precio;
    private Stand stand;
    private List<CategoriaProducto> categorias;

    public ProductoDTO(Producto producto) {
        this.id =producto.getId();
        this.nombre = producto.getNombre();
        Descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.stand = producto.getStand();
        this.categorias = producto.getCategorias();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public Stand getStand() {
        return stand;
    }

    public List<CategoriaProducto> getCategorias() {
        return categorias;
    }
}
