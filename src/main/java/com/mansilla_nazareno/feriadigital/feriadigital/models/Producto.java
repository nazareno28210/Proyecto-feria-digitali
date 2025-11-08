package com.mansilla_nazareno.feriadigital.feriadigital.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private String Descripcion;
    private double precio;

    @ManyToOne
    @JoinColumn(name = "stand_id")
    @JsonIgnoreProperties("productos")
    private Stand stand; // cada producto pertenece a un stand

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("productos")
    private List<CategoriaProducto> categorias; // un producto puede tener varias categorías


    public Producto(){}
    public Producto(double precio, String descripcion, String nombre) {
        this.Descripcion = descripcion;
        this.nombre = nombre;
        this.precio = precio;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setCategorias(List<CategoriaProducto> categorias) {
        this.categorias = categorias;
    }

    public void setStand(Stand stand) {
        this.stand = stand;
    }

    public Stand getStand() {
        return stand;
    }

    public List<CategoriaProducto> getCategorias() {
        return categorias;
    }

}
