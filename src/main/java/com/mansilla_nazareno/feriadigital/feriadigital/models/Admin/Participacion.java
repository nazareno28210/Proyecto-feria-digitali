package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoParticipacion;
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

    private Integer numeroStand;

    @Enumerated(EnumType.STRING)
    private EstadoParticipacion estado;

    private Double ventas;

    public Participacion() {}

    public Participacion(Feria feria, Stand stand, Integer numeroStand, EstadoParticipacion estado) {
        this.feria = feria;
        this.stand = stand;
        this.numeroStand = numeroStand;
        this.estado = estado;
    }

    // Getters y setters

    public int getId() { return id; }

    public EstadoParticipacion getEstado() {return estado;}
    public void setEstado(EstadoParticipacion estado) {this.estado = estado;}


    public void setFeria(Feria feria) {this.feria = feria;}
    public Feria getFeria() {return feria;}

    public Stand getStand() { return stand; }
    public void setStand(Stand stand) { this.stand = stand; }

    public Integer getNumeroStand() { return numeroStand; }
    public void setNumeroStand(Integer numeroStand) { this.numeroStand = numeroStand; }

    public Double getVentas() { return ventas; }
    public void setVentas(Double ventas) { this.ventas = ventas; }
}
