package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoPago; // 🟢 Importamos el Enum financiero

public class ParticipacionDTO {

    private int id;

    private Integer feriaId;
    private String feria;

    private Integer standId;
    private String stand; // Nombre del emprendimiento

    private Integer numeroStand; // Mesa o lugar físico

    private EstadoParticipacion estado;

    // 🟢 NUEVOS CAMPOS FINANCIEROS
    private EstadoPago estadoPago;
    private Double montoAbonado;



    public ParticipacionDTO(Participacion participacion) {
        this.id = participacion.getId();

        // Mapeamos de forma segura por si la feria o el stand vienen nulos
        if (participacion.getFeria() != null) {
            this.feriaId = participacion.getFeria().getId();
            this.feria = participacion.getFeria().getNombre();
        }

        if (participacion.getStand() != null) {
            this.standId = participacion.getStand().getId();
            this.stand = participacion.getStand().getNombre();
        }

        this.numeroStand = participacion.getNumeroStand();
        this.estado = participacion.getEstado();

        // 🟢 MAPEO DE LA PLATA
        this.estadoPago = participacion.getEstadoPago();
        this.montoAbonado = participacion.getMontoAbonado();
    }

    // --- GETTERS (Indispensables para que Spring arme el JSON) ---

    public int getId() { return id; }
    public Integer getFeriaId() { return feriaId; }
    public String getFeria() { return feria; }
    public Integer getStandId() { return standId; }
    public String getStand() { return stand; }
    public Integer getNumeroStand() { return numeroStand; }
    public EstadoParticipacion getEstado() { return estado; }
    public EstadoPago getEstadoPago() { return estadoPago; }
    public Double getMontoAbonado() { return montoAbonado; }

}