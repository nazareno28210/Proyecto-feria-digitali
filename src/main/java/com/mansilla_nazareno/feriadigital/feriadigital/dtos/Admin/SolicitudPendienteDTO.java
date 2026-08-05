package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.EstadoSolicitud;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.SolicitudParaFeriante;

public class SolicitudPendienteDTO {
    private int id;
    private String nombreUsuario;
    private String apellidoUsuario;
    private String emailUsuario;
    private String nombreEmprendimiento;
    private String descripcion;
    private String telefono;
    private String emailEmprendimiento;
    private EstadoSolicitud estado;
    private String motivoRechazo;

    public SolicitudPendienteDTO(){}
    public SolicitudPendienteDTO(SolicitudParaFeriante solicitud) {
        this.id = solicitud.getId();
        this.nombreUsuario = solicitud.getUsuario().getNombre();
        this.apellidoUsuario = solicitud.getUsuario().getApellido();
        this.emailUsuario = solicitud.getUsuario().getEmail();
        this.nombreEmprendimiento = solicitud.getNombreEmprendimiento();
        this.descripcion = solicitud.getDescripcion();
        this.telefono = solicitud.getTelefono();
        this.emailEmprendimiento = solicitud.getEmailEmprendimiento();
        this.estado = solicitud.getEstado();
        this.motivoRechazo = solicitud.getMotivoRechazo();
    }


    // Getters necesarios para que Spring envíe el JSON
    public int getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getApellidoUsuario() { return apellidoUsuario; }
    public String getEmailUsuario() { return emailUsuario; }
    public String getNombreEmprendimiento() { return nombreEmprendimiento; }
    public String getDescripcion() { return descripcion; }
    public String getTelefono() { return telefono; }
    public String getEmailEmprendimiento() { return emailEmprendimiento; }
    public EstadoSolicitud getEstado() { return estado; }
    public String getMotivoRechazo() { return motivoRechazo; }
}