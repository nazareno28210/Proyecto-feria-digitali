package com.mansilla_nazareno.feriadigital.feriadigital.models.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Persona;
import jakarta.persistence.*;

@Entity
@Table(name = "participante_persona")
@PrimaryKeyJoinColumn(name = "id_participante")
@DiscriminatorValue("PERSONA")
public class ParticipantePersona extends Participante {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    public ParticipantePersona() {
    }

    public ParticipantePersona(String nivelRegistracion, String estadoGeneral, Persona persona) {
        super(nivelRegistracion, estadoGeneral);
        this.persona = persona;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }
}
