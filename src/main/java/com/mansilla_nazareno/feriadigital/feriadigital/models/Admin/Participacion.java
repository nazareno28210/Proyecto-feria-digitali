package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion; // 🟢 Enum 1 (Logística)
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoPago;          // 🟢 Enum 2 (Dinero)
import jakarta.persistence.*;

@Entity
@Table(name = "participacion")
public class Participacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "feria_id", nullable = false)
    @JsonIgnoreProperties("participaciones")
    private Feria feria;

    @ManyToOne
    @JoinColumn(name = "stand_id", nullable = false)
    @JsonIgnoreProperties("participaciones")
    private Stand stand;

    private Integer numeroStand; // La ubicación (Ej: Mesa 12)

    // 🚦 SEMÁFORO 1: Logística
    @Enumerated(EnumType.STRING)
    private EstadoParticipacion estado;

    // 🚦 SEMÁFORO 2: Dinero
    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.DEBE;

    private Double montoAbonado = 0.0;

    public Participacion() {}

    public Participacion(Feria feria, Stand stand, Integer numeroStand, EstadoParticipacion estado) {
        this.feria = feria;
        this.stand = stand;
        this.numeroStand = numeroStand;
        this.estado = estado;
        this.estadoPago = EstadoPago.DEBE; // Siempre arranca debiendo
        this.montoAbonado = 0.0;
    }

    // --- Getters y setters ---

    public int getId() { return id; }

    public EstadoParticipacion getEstado() { return estado; }
    public void setEstado(EstadoParticipacion estado) { this.estado = estado; }

    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }

    public Double getMontoAbonado() { return montoAbonado; }
    public void setMontoAbonado(Double montoAbonado) { this.montoAbonado = montoAbonado; }

    public void setFeria(Feria feria) { this.feria = feria; }
    public Feria getFeria() { return feria; }

    public Stand getStand() { return stand; }
    public void setStand(Stand stand) { this.stand = stand; }

    public Integer getNumeroStand() { return numeroStand; }
    public void setNumeroStand(Integer numeroStand) { this.numeroStand = numeroStand; }

}