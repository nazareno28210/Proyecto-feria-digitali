package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante;

// Este DTO solo lleva los campos que el feriante puede editar de sí mismo.
public class FerianteUpdateDTO {
    private String telefono;
    private String emailEmprendimiento;

    // Getters (necesarios para que Spring lea el JSON)
    public String getTelefono() { return telefono; }
    public String getEmailEmprendimiento() { return emailEmprendimiento; }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setEmailEmprendimiento(String emailEmprendimiento) {
        this.emailEmprendimiento = emailEmprendimiento;
    }
}