package com.mansilla_nazareno.feriadigital.feriadigital.models;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String token;

    private LocalDateTime fechaExpiracion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public PasswordResetToken() {}

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}