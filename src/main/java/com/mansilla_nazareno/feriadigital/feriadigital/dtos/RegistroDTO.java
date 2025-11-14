package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

public class RegistroDTO {
    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;
    private String confirmContrasena;

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getContrasena() { return contrasena; }
    public String getConfirmContrasena() { return confirmContrasena; }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    public void setConfirmContrasena(String confirmContrasena) {
        this.confirmContrasena = confirmContrasena;
    }
}