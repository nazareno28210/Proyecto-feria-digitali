package com.mansilla_nazareno.feriadigital.feriadigital.models;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;
    private LocalDate fechaRegistro;

    @Enumerated(EnumType.STRING)
    private UserEstate userEstate;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @OneToOne(mappedBy = "usuario")
    private Feriante feriante;

    public User() {}

    public User(String nombre, String apellido, String email, String contrasena, UserType userType, UserEstate userEstate) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasena = contrasena;
        this.fechaRegistro = LocalDate.now();
        this.userEstate = userEstate;
        this.userType = userType;
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

    public UserEstate getUserEstate() {
        return userEstate;
    }

    public void setUserEstate(UserEstate userEstate) {
        this.userEstate = userEstate;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}