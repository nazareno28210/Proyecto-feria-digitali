package com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import java.util.List;
import java.util.stream.Collectors;

public class UsuarioDTO {
    private int id;
    private String nombre;
    private String apellido;
    private String email;
    private boolean activo;
    private List<String> roles;
    private String imagenUrl;

    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getIdUsuario();
        if (usuario.getPersona() != null) {
            this.nombre = usuario.getPersona().getNombre();
            this.apellido = usuario.getPersona().getApellido();
            this.imagenUrl = usuario.getPersona().getImagenUrl();
        }
        this.email = usuario.getNombreUsuario();
        this.activo = usuario.isActivo();
        this.roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toList());
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

    public boolean isActivo() {
        return activo;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }
}
