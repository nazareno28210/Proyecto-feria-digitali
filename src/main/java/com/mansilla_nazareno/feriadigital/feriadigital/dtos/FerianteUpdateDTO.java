package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

// Este DTO solo lleva los campos que el feriante puede editar de sí mismo.
public class FerianteUpdateDTO {
    private String nombreEmprendimiento;
    private String descripcion;
    private String telefono;
    private String emailEmprendimiento;

    // Getters (necesarios para que Spring lea el JSON)
    public String getNombreEmprendimiento() { return nombreEmprendimiento; }
    public String getDescripcion() { return descripcion; }
    public String getTelefono() { return telefono; }
    public String getEmailEmprendimiento() { return emailEmprendimiento; }
}