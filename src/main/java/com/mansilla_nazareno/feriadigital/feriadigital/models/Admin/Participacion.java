package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "participacion")
public class Participacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "edicion_id", nullable = false)
    @JsonIgnoreProperties("participaciones")
    private EdicionFeria edicion;

    @ManyToOne
    @JoinColumn(name = "stand_id", nullable = false)
    @JsonIgnoreProperties("participaciones")
    private Stand stand;

    @ManyToOne
    @JoinColumn(name = "espacio_id")
    private Espacio espacio;

    @Enumerated(EnumType.STRING)
    private EstadoParticipacion estado;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.DEBE;

    private Double montoAbonado = 0.0;

    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    public Participacion() {}

    public Participacion(EdicionFeria edicion, Stand stand, Espacio espacio, EstadoParticipacion estado) {
        this.edicion = edicion;
        this.stand = stand;
        this.espacio = espacio;
        this.estado = estado;
        this.estadoPago = EstadoPago.DEBE;
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

    public EdicionFeria getEdicion() { return edicion; }
    public void setEdicion(EdicionFeria edicion) { this.edicion = edicion; }

    public Stand getStand() { return stand; }
    public void setStand(Stand stand) { this.stand = stand; }

    public Espacio getEspacio() { return espacio; }
    public void setEspacio(Espacio espacio) { this.espacio = espacio; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}