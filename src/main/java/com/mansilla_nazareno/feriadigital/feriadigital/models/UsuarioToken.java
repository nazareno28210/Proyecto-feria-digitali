package com.mansilla_nazareno.feriadigital.feriadigital.models;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_token")
public class UsuarioToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoToken tipoToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    private boolean usado = false;

    private LocalDateTime fechaUso;

    public UsuarioToken() {}

    public UsuarioToken(String token, TipoToken tipoToken, Usuario usuario) {
        this.token = token;
        this.tipoToken = tipoToken;
        this.usuario = usuario;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaExpiracion = tipoToken.calcularFechaExpiracion();
        this.usado = false;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TipoToken getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(TipoToken tipoToken) {
        this.tipoToken = tipoToken;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }

    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    public void setFechaUso(LocalDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }

    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(this.fechaExpiracion);
    }

    public boolean isValido() {
        return !this.usado && !isExpirado();
    }
}
