package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import jakarta.persistence.*;

@Entity
@Table(name = "espacios")
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEspacio estado;

    @ManyToOne
    @JoinColumn(name = "edicion_id", nullable = false)
    private EdicionFeria edicion;


    public Espacio() {}

    public Espacio(String nombre, Double precio, EstadoEspacio estado, EdicionFeria edicion) {
        this.nombre = nombre;
        this.precio = precio;
        this.estado = estado;
        this.edicion = edicion;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public EstadoEspacio getEstado() { return estado; }
    public void setEstado(EstadoEspacio estado) { this.estado = estado; }

    public EdicionFeria getEdicion() { return edicion; }
    public void setEdicion(EdicionFeria edicion) { this.edicion = edicion; }


}
