package com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;
    private LocalDate fechaRegistro;

    @Enumerated(EnumType.STRING)
    private EstadoUsuario estadoUsuario;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipoUsuario;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "imagen_public_id")
    private String imagenPublicId;

    public Usuario() {}

    public Usuario(String nombre, String apellido, String email, String contrasena, EstadoUsuario estadoUsuario) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasena = contrasena;
        this.fechaRegistro = LocalDate.now();
        this.estadoUsuario = estadoUsuario;
        this.tipoUsuario = TipoUsuario.NORMAL;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public EstadoUsuario getEstadoUsuario() {
        return estadoUsuario;
    }

    public void setUserEstate(EstadoUsuario estadoUsuario) {
        this.estadoUsuario = estadoUsuario;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getImagenUrl() { return imagenUrl; }

    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getImagenPublicId() {return imagenPublicId;}

    public void setImagenPublicId(String imagenPublicId) {this.imagenPublicId = imagenPublicId;}
}