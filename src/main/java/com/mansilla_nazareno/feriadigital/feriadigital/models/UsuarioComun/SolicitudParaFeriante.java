package com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun;

import jakarta.persistence.*;

@Entity
public class SolicitudParaFeriante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    private Usuario usuario;

    private boolean aprobada = false;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(length = 1000)
    private String motivoRechazo;

    // 🆕 CAMPOS DEL FORMULARIO (Borrador del futuro Feriante)
    private String nombreEmprendimiento;
    private String descripcion;
    private String telefono;
    private String emailEmprendimiento;

    public SolicitudParaFeriante() {}

    public SolicitudParaFeriante(Usuario usuario, String nombreEmprendimiento, String descripcion, String telefono, String emailEmprendimiento) {
        this.usuario = usuario;
        this.nombreEmprendimiento = nombreEmprendimiento;
        this.descripcion = descripcion;
        this.telefono = telefono;
        this.emailEmprendimiento = emailEmprendimiento;
        this.aprobada = false; // Por defecto nace no aprobada
        this.estado = EstadoSolicitud.PENDIENTE;
        this.motivoRechazo = null;
    }

    // Getters y Setters necesarios...

    public int getId() {
        return id;
    }

    public Usuario getUsuario() { return usuario; }

    public boolean isAprobada() { return aprobada; }
    public void setAprobada(boolean aprobada) {
        this.aprobada = aprobada;
        if (aprobada) {
            this.estado = EstadoSolicitud.APROBADA;
        }
    }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
        if (estado == EstadoSolicitud.APROBADA) {
            this.aprobada = true;
        } else if (estado == EstadoSolicitud.RECHAZADA) {
            this.aprobada = false;
        }
    }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public String getNombreEmprendimiento() { return nombreEmprendimiento; }
    public void setNombreEmprendimiento(String nombreEmprendimiento) { this.nombreEmprendimiento = nombreEmprendimiento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmailEmprendimiento() { return emailEmprendimiento; }
    public void setEmailEmprendimiento(String emailEmprendimiento) { this.emailEmprendimiento = emailEmprendimiento; }
}