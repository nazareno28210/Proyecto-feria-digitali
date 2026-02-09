package com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;

import jakarta.persistence.*;

@Entity
public class CategoriaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true) // El nombre debe ser único
    private String nombre;

    private String descripcion;

    public CategoriaProducto(){}
    public CategoriaProducto(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setValue(String descripcion) { this.descripcion = descripcion; }
}