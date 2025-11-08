package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mansilla_nazareno.feriadigital.feriadigital.models.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Producto;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class CategoriaProductoDTO {
    private int id;
    private String nombre;
    private String descripcion;


    public CategoriaProductoDTO(){}
    public CategoriaProductoDTO(CategoriaProducto categoriaProducto){
        this.id=categoriaProducto.getId();
        this.descripcion=categoriaProducto.getDescripcion();
        this.nombre=categoriaProducto.getNombre();
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


}
