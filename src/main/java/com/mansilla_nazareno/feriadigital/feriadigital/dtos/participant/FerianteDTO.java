package com.mansilla_nazareno.feriadigital.feriadigital.dtos.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.fair.StandDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Participante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.ParticipantePersona;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.ParticipanteEmpresa;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Empresa;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;

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

    // 1. Constructor Principal
    public FerianteDTO(Participante participante) {
        this(participante, false);
    }

    // 2. CONSTRUCTOR ANTI-RECURSIVIDAD
    public FerianteDTO(Participante participante, boolean ignorarStand) {
        if (participante == null) return;

        this.id = participante.getId();
        this.fechaRegistro = participante.getFechaRegistro();
        this.estadoUsuario = participante.getUserEstate();
        this.usuario = participante.getUsuario();
        this.telefono = participante.getTelefono();

        // Lógica de mapeo según tipo de participante para campos antiguos
        if (participante instanceof Feriante) {
            Feriante oldFeriante = (Feriante) participante;
            this.nombreEmprendimiento = oldFeriante.getNombreEmprendimiento();
            this.descripcion = oldFeriante.getDescripcion();
            this.emailEmprendimiento = oldFeriante.getEmailEmprendimiento();
        } else if (participante instanceof ParticipantePersona) {
            ParticipantePersona partPersona = (ParticipantePersona) participante;
            if (partPersona.getPersona() != null) {
                this.nombreEmprendimiento = partPersona.getPersona().getNombre() + " " + partPersona.getPersona().getApellido();
                this.descripcion = "Feriante Individual";
                this.emailEmprendimiento = partPersona.getPersona().getUsuario() != null 
                        ? partPersona.getPersona().getUsuario().getNombreUsuario() 
                        : null;
            }
        } else if (participante instanceof ParticipanteEmpresa) {
            ParticipanteEmpresa partEmpresa = (ParticipanteEmpresa) participante;
            Empresa emp = partEmpresa.getEmpresa();
            if (emp != null) {
                this.nombreEmprendimiento = emp.getNombreFantasia();
                this.descripcion = emp.getRazonSocial();
                this.emailEmprendimiento = emp.getEmail();
                this.telefono = emp.getTelefono();
            }
        }

        // Cargar stand
        if (!ignorarStand && participante.getStand() != null) {
            this.stand = new StandDTO(participante.getStand(), true);
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