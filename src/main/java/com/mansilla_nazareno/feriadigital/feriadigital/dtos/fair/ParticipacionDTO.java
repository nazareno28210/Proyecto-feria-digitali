package com.mansilla_nazareno.feriadigital.feriadigital.dtos.fair;
import com.mansilla_nazareno.feriadigital.feriadigital.models.feria.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.feria.EstadoParticipacion;

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
