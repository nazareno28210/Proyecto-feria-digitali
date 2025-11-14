package com.mansilla_nazareno.feriadigital.feriadigital.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class SolicitudParaFeriante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    private Usuario usuario;

    private boolean aprobada = false;

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
    }

    // Getters y Setters necesarios...

    public int getId() {
        return id;
    }

    public Usuario getUsuario() { return usuario; }
    public boolean isAprobada() { return aprobada; }
    public void setAprobada(boolean aprobada) { this.aprobada = aprobada; }
    public String getNombreEmprendimiento() { return nombreEmprendimiento; }
    public String getDescripcion() { return descripcion; }
    public String getTelefono() { return telefono; }
    public String getEmailEmprendimiento() { return emailEmprendimiento; }
}