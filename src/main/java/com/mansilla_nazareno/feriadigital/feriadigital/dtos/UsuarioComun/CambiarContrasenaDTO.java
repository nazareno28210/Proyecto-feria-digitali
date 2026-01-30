package com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun;

public class CambiarContrasenaDTO {

    private String contrasenaActual;
    private String nuevacontrasena;
    private String confirmarNuevacontrasena;


    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public void setContrasenaActual(String contrasenaActual) {
        this.contrasenaActual = contrasenaActual;
    }

    public String getNuevacontrasena() {
        return nuevacontrasena;
    }

    public void setNuevacontrasena(String nuevacontrasena) {
        this.nuevacontrasena = nuevacontrasena;
    }

    public String getConfirmarNuevacontrasena() {
        return confirmarNuevacontrasena;
    }

    public void setConfirmarNuevacontrasena(String confirmarNuevacontrasena) {
        this.confirmarNuevacontrasena = confirmarNuevacontrasena;
    }
}
