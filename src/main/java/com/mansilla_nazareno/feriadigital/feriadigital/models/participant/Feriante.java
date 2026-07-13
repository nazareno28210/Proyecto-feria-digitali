package com.mansilla_nazareno.feriadigital.feriadigital.models.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.ParticipantePersona;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.EstadoUsuario;
import jakarta.persistence.*;

@Entity
@Table(name = "feriante_compat")
@PrimaryKeyJoinColumn(name = "id_participante")
@DiscriminatorValue("FERIANTE")
@Deprecated
public class Feriante extends ParticipantePersona {

    @Column(name = "nombre_emprendimiento", length = 150)
    private String nombreEmprendimiento;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email_emprendimiento", length = 100)
    private String emailEmprendimiento;

    public Feriante() {
    }

    public Feriante(String nombreEmprendimiento, String descripcion, String telefono, String emailEmprendimiento, EstadoUsuario estadoUsuario) {
        super("MONOTRIBUTO", estadoUsuario == EstadoUsuario.ACTIVO ? "ACTIVO" : "PENDIENTE", null);
        this.nombreEmprendimiento = nombreEmprendimiento;
        this.descripcion = descripcion;
        this.telefono = telefono;
        this.emailEmprendimiento = emailEmprendimiento;
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

    public void setUsuario(Usuario usuario) {
        if (usuario != null) {
            setPersona(usuario.getPersona());
        }
    }
}
