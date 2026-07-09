package com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun;

import jakarta.persistence.*;

@Entity
@Table(name = "participante_empresa")
@PrimaryKeyJoinColumn(name = "id_participante")
@DiscriminatorValue("EMPRESA")
public class ParticipanteEmpresa extends Participante {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    public ParticipanteEmpresa() {
    }

    public ParticipanteEmpresa(String nivelRegistracion, String estadoGeneral, Empresa empresa) {
        super(nivelRegistracion, estadoGeneral);
        this.empresa = empresa;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
}
