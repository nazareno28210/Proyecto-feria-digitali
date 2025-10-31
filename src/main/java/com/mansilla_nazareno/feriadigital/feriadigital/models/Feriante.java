package com.mansilla_nazareno.feriadigital.feriadigital.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private EstadoUsuario estadoUsuario;

    @OneToOne
    @JoinColumn(name = "fk_id_usuario", referencedColumnName = "id")
    private Usuario usuario;

    @OneToOne(mappedBy = "feriante")
    @JsonIgnoreProperties("feriante")
    private Stand stand;

    public Feriante() {}

    public Feriante(String nombreEmprendimiento, String descripcion, String telefono, String emailEmprendimiento,
             EstadoUsuario estadoUsuario) {
        this.nombreEmprendimiento = nombreEmprendimiento;
        this.descripcion = descripcion;
        this.telefono = telefono;
        this.emailEmprendimiento = emailEmprendimiento;
        this.fechaRegistro =  LocalDate.now();
        this.estadoUsuario = estadoUsuario;

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
        this.emailEmprendimiento = emailEmprendimiento;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }


    public EstadoUsuario getUserEstate() {
        return estadoUsuario;
    }

    public void setUserEstate(EstadoUsuario estadoUsuario) {
        this.estadoUsuario = estadoUsuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (usuario.getTipoUsuario() == TipoUsuario.NORMAL) {
            usuario.setTipoUsuario(TipoUsuario.FERIANTE);
        }
        this.usuario = usuario;
    }


    public TipoUsuario getTipoUsuario() {
        if (this.usuario != null) {
            return this.usuario.getTipoUsuario();
        }
        return null;
    }
    // getters y setters
    public Stand getStand() {
        return stand;
    }

    public void setStand(Stand stand) {
        this.stand = stand;
    }
}
