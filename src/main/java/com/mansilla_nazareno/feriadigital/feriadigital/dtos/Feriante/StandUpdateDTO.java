package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante;

// Este DTO solo lleva los campos que el feriante puede editar de su stand.
public class StandUpdateDTO {
    private String nombre;
    private String descripcion;

    // Getters
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}