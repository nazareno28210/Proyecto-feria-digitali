package com.mansilla_nazareno.feriadigital.feriadigital.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Stand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private String descripcion;
    @ManyToOne
    @JoinColumn(name = "feria_id", referencedColumnName = "id")
    @JsonIgnoreProperties("stands")

    private Feria feria; // cada stand pertenece a una feria

    @OneToMany(mappedBy = "stand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("stand")
    private List<Producto> productos; // un stand puede tener muchos productos

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "feriante_id") // crea la columna en la tabla Stand
    @JsonIgnoreProperties("stands")
    private Feriante feriante;

    public Stand(){}
    public Stand(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public void setFeria(Feria feria) {
        this.feria = feria;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public Feria getFeria() {
        return feria;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public Feriante getFeriante() {
        return feriante;
    }

    public void setFeriante(Feriante feriante) {
        this.feriante = feriante;
    }

}
