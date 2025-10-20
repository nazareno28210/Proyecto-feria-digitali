package com.mansilla_nazareno.feriadigital.feriadigital.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Stand {
    @Id
    private int id;
    private String nombre;
    private String descripcion;
    @ManyToOne
    @JoinColumn(name = "feria_id")
    private Feria feria; // cada stand pertenece a una feria

    @OneToMany(mappedBy = "stand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Producto> productos; // un stand puede tener muchos productos

    public Stand(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
