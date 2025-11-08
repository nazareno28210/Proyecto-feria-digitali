package com.mansilla_nazareno.feriadigital.feriadigital.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class SolicitudParaFeriante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "fk_usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    private LocalDate fechaSolicitud;
    private boolean aprobada; // false = pendiente, true = aprobada

    public SolicitudParaFeriante() {}

    public SolicitudParaFeriante(Usuario usuario) {
        this.usuario = usuario;
        this.fechaSolicitud = LocalDate.now();
        this.aprobada = false;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public boolean isAprobada() {
        return aprobada;
    }

    public void setAprobada(boolean aprobada) {
        this.aprobada = aprobada;
    }
}
