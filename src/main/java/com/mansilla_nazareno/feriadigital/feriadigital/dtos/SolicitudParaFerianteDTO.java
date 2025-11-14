package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.SolicitudParaFeriante;

public class SolicitudParaFerianteDTO {
    private String nombreEmprendimiento;
    private String descripcion;
    private String telefono;
    private String emailEmprendimiento;
    private  SolicitudParaFeriante s;

    public SolicitudParaFerianteDTO(){}
    public SolicitudParaFerianteDTO(SolicitudParaFeriante s) {
        this.nombreEmprendimiento=s.getNombreEmprendimiento();
        this.descripcion=s.getDescripcion();
        this.telefono=s.getTelefono();
        this.emailEmprendimiento=s.getEmailEmprendimiento();
    }

    public String getNombreEmprendimiento() {
        return nombreEmprendimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmailEmprendimiento() {
        return emailEmprendimiento;
    }

    public SolicitudParaFeriante getS() {
        return s;
    }

    public void setNombreEmprendimiento(String nombreEmprendimiento) {
        this.nombreEmprendimiento = nombreEmprendimiento;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setEmailEmprendimiento(String emailEmprendimiento) {
        this.emailEmprendimiento = emailEmprendimiento;
    }

}