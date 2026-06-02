package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion; // 🚦 Semáforo 1 (Logística)
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoPago;          // 🚦 Semáforo 2 (Dinero)
import jakarta.persistence.*;

@Entity
@Table(name = "participacion")
public class Participacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "edicion_id", nullable = false) // 🟢 CAMBIO CRÍTICO: Ahora apunta a la edición del evento
    @JsonIgnoreProperties("participaciones")
    private EdicionFeria edicion;

    @ManyToOne
    @JoinColumn(name = "stand_id", nullable = false)
    @JsonIgnoreProperties("participaciones")
    private Stand stand;

    private Integer numeroStand; // La ubicación asignada (Ej: Mesa 12)

    @Enumerated(EnumType.STRING)
    private EstadoParticipacion estado;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.DEBE;

    private Double montoAbonado = 0.0;
    private Integer numeroStandPreferido; // La sugerencia del feriante

    public Participacion() {}

    // Constructor actualizado con EdicionFeria
    public Participacion(EdicionFeria edicion, Stand stand, Integer numeroStand, EstadoParticipacion estado, Integer numeroStandPreferido) {
        this.edicion = edicion;
        this.stand = stand;
        this.numeroStand = numeroStand;
        this.estado = estado;
        this.numeroStandPreferido = numeroStandPreferido;
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

    // 🟢 CAMBIO: Getters y Setters de Edición en vez de Feria
    public void setEdicion(EdicionFeria edicion) { this.edicion = edicion; }
    public EdicionFeria getEdicion() { return edicion; }

    public Stand getStand() { return stand; }
    public void setStand(Stand stand) { this.stand = stand; }

    public Integer getNumeroStand() { return numeroStand; }
    public void setNumeroStand(Integer numeroStand) { this.numeroStand = numeroStand; }

    public Integer getNumeroStandPreferido() { return numeroStandPreferido; }
    public void setNumeroStandPreferido(Integer numeroStandPreferido) { this.numeroStandPreferido = numeroStandPreferido; }
}