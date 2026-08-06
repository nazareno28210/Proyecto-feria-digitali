package com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recordatorios_edicion", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"usuario_id", "edicion_id"})
})
public class RecordatorioEdicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "edicion_id", nullable = false)
    private EdicionFeria edicion;

    @Column(name = "fecha_suscripcion", nullable = false)
    private LocalDateTime fechaSuscripcion;

    @Column(name = "notificado", nullable = false)
    private boolean notificado = false;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public RecordatorioEdicion() {
        this.fechaSuscripcion = LocalDateTime.now();
        this.activo = true;
    }

    public RecordatorioEdicion(Usuario usuario, EdicionFeria edicion) {
        this.usuario = usuario;
        this.edicion = edicion;
        this.fechaSuscripcion = LocalDateTime.now();
        this.notificado = false;
        this.activo = true;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public EdicionFeria getEdicion() {
        return edicion;
    }

    public void setEdicion(EdicionFeria edicion) {
        this.edicion = edicion;
    }

    public LocalDateTime getFechaSuscripcion() {
        return fechaSuscripcion;
    }

    public void setFechaSuscripcion(LocalDateTime fechaSuscripcion) {
        this.fechaSuscripcion = fechaSuscripcion;
    }

    public boolean isNotificado() {
        return notificado;
    }

    public void setNotificado(boolean notificado) {
        this.notificado = notificado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}

