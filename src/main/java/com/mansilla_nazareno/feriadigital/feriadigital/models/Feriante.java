package com.mansilla_nazareno.feriadigital.feriadigital.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Feriante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombreEmprendimiento;
    private String descripcion;
    private String telefono;
    private String emailEmprendimiento;
    private LocalDate fechaRegistro;

    @Enumerated(EnumType.STRING)
    private UserEstate userEstate;

    @OneToOne
    @JoinColumn(name = "fk_id_usuario", referencedColumnName = "id")
    private User usuario;

    public Feriante() {}

    public Feriante(String nombreEmprendimiento, String descripcion, String telefono, String emailEmprendimiento,
                    LocalDate fechaRegistro, UserEstate userEstate, User usuario) {
        this.nombreEmprendimiento = nombreEmprendimiento;
        this.descripcion = descripcion;
        this.telefono = telefono;
        this.emailEmprendimiento = emailEmprendimiento;
        this.fechaRegistro =  LocalDate.now();
        this.userEstate = userEstate;
        this.usuario = usuario;
    }

    public int getId() {
        return id;
    }

    public String getNombreEmprendimiento() {
        return nombreEmprendimiento;
    }

    public void setNombreEmprendimiento(String nombreEmprendimiento) {
        this.nombreEmprendimiento = nombreEmprendimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmailEmprendimiento() {
        return emailEmprendimiento;
    }

    public void setEmailEmprendimiento(String emailEmprendimiento) {
        this.emailEmprendimiento = this.emailEmprendimiento;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public UserEstate getUserEstate() {
        return userEstate;
    }

    public void setUserEstate(UserEstate userEstate) {
        this.userEstate = userEstate;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public UserType getUserType() {
        if (this.usuario != null) {
            return this.usuario.getUserType();
        }
        return null;
    }
}
