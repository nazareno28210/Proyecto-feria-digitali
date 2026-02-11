package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;

public class FeriaSelectorDTO {
    private int id;
    private String nombre;

    public FeriaSelectorDTO(Feria feria) {
        this.id = feria.getId(); // cite: source 53
        this.nombre = feria.getNombre(); // cite: source 65
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
}