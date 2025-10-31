package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;

import java.time.LocalDate;

public class UsuarioDTO {
    private int id;
    private  String nombre;
    private  String apellido;
    private  String email;
    private  String contrasena;
    private EstadoUsuario estadoUsuario;
    private LocalDate DayRegistrer;
    private TipoUsuario tipoUsuario;

    public UsuarioDTO(Usuario usuario) {
        this.id= usuario.getId();
        this.nombre = usuario.getNombre();
        this.apellido = usuario.getApellido();
        this.email = usuario.getEmail();
        this.contrasena = usuario.getContrasena();
        this.estadoUsuario = usuario.getEstadoUsuario();
        this.DayRegistrer = usuario.getFechaRegistro();
        this.tipoUsuario = usuario.getTipoUsuario();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public EstadoUsuario getEstadoUsuario() {
        return estadoUsuario;
    }

    public LocalDate getDayRegistrer() {
        return DayRegistrer;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

}
