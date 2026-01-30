package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.CategoriaProducto;

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
