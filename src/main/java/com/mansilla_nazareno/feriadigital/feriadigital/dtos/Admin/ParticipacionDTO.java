package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoParticipacion;

public class ParticipacionDTO {


    private int id;
    private String feria;
    private String stand;
    private Integer numeroStand;
    private EstadoParticipacion estado;

    private Double ventas;
    public ParticipacionDTO(Participacion participacion) {
        this.estado=participacion.getEstado();
        this.numeroStand=participacion.getNumeroStand();

    }

}
