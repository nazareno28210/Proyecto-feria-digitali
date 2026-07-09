package com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_seguridad")
public class TokenSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private int idToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "token", length = 255, nullable = false, unique = true)
    private String token;

    @Column(name = "tipo_token", length = 50, nullable = false)
    private String tipoToken; // Ej: RECUPERAR_PASSWORD, VERIFICAR_EMAIL

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    public TokenSeguridad() {
    }

    public TokenSeguridad(Usuario usuario, String token, String tipoToken, LocalDateTime fechaExpiracion) {
        this.usuario = usuario;
        this.token = token;
        this.tipoToken = tipoToken;
        this.fechaExpiracion = fechaExpiracion;
    }

    public int getIdToken() {
        return idToken;
    }

    public void setIdToken(int idToken) {
        this.idToken = idToken;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.fechaExpiracion);
    }
}
