package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.SolicitudParaFeriante;

public class SolicitudPendienteDTO {
    private int id;
    private String nombreUsuario;
    private String apellidoUsuario;
    private String emailUsuario;
    private String nombreEmprendimiento;
    // private String fechaSolicitud; // Descomenta si agregaste fecha a la entidad
    public SolicitudPendienteDTO(){}
    public SolicitudPendienteDTO(SolicitudParaFeriante solicitud) {
        this.id = solicitud.getId();
        this.nombreUsuario = solicitud.getUsuario().getNombre();
        this.apellidoUsuario = solicitud.getUsuario().getApellido();
        this.emailUsuario = solicitud.getUsuario().getEmail();
        this.nombreEmprendimiento = solicitud.getNombreEmprendimiento();
    }

    // Getters necesarios para que Spring envíe el JSON
    public int getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getApellidoUsuario() { return apellidoUsuario; }
    public String getEmailUsuario() { return emailUsuario; }
    public String getNombreEmprendimiento() { return nombreEmprendimiento; }
}