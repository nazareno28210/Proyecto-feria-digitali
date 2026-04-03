package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.StandDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;

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
    private StandDTO stand;

    // 1. Constructor Principal (Por defecto, intenta cargar el stand)
    public FerianteDTO(Feriante feriante) {
        this(feriante, false);
    }

    // 2. 🟢 CONSTRUCTOR ANTI-RECURSIVIDAD: Permite decidir si cargar o no el Stand
    public FerianteDTO(Feriante feriante, boolean ignorarStand) {
        this.id = feriante.getId();
        this.nombreEmprendimiento = feriante.getNombreEmprendimiento();
        this.descripcion = feriante.getDescripcion();
        this.telefono = feriante.getTelefono();
        this.emailEmprendimiento = feriante.getEmailEmprendimiento();
        this.fechaRegistro = feriante.getFechaRegistro();
        this.estadoUsuario = feriante.getUserEstate();
        this.usuario = feriante.getUsuario();

        // Solo cargamos el stand si explícitamente se permite
        if (!ignorarStand && feriante.getStand() != null) {
            // Llamamos al constructor del Stand pasándole "true" para que no vuelva a cargar al Feriante
            this.stand = new StandDTO(feriante.getStand(), true);
        } else {
            this.stand = null;
        }
    }

    // --- Getters ---
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

    public StandDTO getStand() {
        return stand;
    }
}