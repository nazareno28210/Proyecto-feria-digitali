package com.mansilla_nazareno.feriadigital.feriadigital.dtos;

import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Usuario;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import java.time.LocalDate;

public class FerianteDTO {

    private int id;
    private String nombreEmprendimiento;
    private String descripcion;
    private String telefono;
    private String emailEmprendimiento;
    private LocalDate fechaRegistro;
    private EstadoUsuario estadoUsuario;
    private Usuario usuario;

    public FerianteDTO(Feriante feriante) {
        this.id = feriante.getId();
        this.nombreEmprendimiento = feriante.getNombreEmprendimiento();
        this.descripcion = feriante.getDescripcion();
        this.telefono = feriante.getTelefono();
        this.emailEmprendimiento = feriante.getEmailEmprendimiento();
        this.fechaRegistro = feriante.getFechaRegistro();
        this.estadoUsuario = feriante.getUserEstate();
        this.usuario = feriante.getUsuario();
    }

    public int getId() {
        return id;
    }

    public String getNombreEmprendimiento() {
        return nombreEmprendimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmailEmprendimiento() {
        return emailEmprendimiento;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public EstadoUsuario getEstadoUsuario() {
        return estadoUsuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
