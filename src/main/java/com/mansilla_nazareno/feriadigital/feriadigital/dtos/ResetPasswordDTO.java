package com.mansilla_nazareno.feriadigital.feriadigital.dtos;


public class ResetPasswordDTO {

    private String token;
    private String nuevaPassword;

    public ResetPasswordDTO() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }



}
