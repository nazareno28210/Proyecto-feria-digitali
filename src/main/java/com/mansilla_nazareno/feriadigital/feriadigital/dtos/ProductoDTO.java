package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Stand;
import jakarta.persistence.*;

import java.util.List;
import java.util.stream.Collectors;

public class ProductoDTO {
    private int id;
    private String nombre;
    private String Descripcion;
    private double precio;
    private List<CategoriaProductoDTO> categorias;
    private String imagen; // NUEVO

    public ProductoDTO(Producto producto) {
        this.id =producto.getId();
        this.nombre = producto.getNombre();
        Descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.categorias = producto.getCategorias()
                .stream()
                .map(CategoriaProductoDTO::new)
                .collect(Collectors.toList());;
        this.imagen = producto.getImagen();
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



    public List<CategoriaProductoDTO> getCategorias() {
        return categorias;
    }
    public String getImagen() {
        return imagen;
    }
}
