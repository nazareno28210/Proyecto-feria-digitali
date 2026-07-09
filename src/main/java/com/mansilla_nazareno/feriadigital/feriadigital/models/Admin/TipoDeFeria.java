package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_de_feria")
public class TipoDeFeria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_feria")
    private int idTipoFeria;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    public TipoDeFeria() {
    }

    public TipoDeFeria(String nombre) {
        this.nombre = nombre;
    }

    public int getIdTipoFeria() {
        return idTipoFeria;
    }

    public void setIdTipoFeria(int idTipoFeria) {
        this.idTipoFeria = idTipoFeria;
    }

    // Método bridge de compatibilidad para getId()
    public int getId() {
        return idTipoFeria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
