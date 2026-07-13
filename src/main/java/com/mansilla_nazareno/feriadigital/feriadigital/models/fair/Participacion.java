package com.mansilla_nazareno.feriadigital.feriadigital.models.fair;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Participante;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "participacion")
public class Participacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participacion")
    private int idParticipacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_edicion_feria", nullable = false)
    @JsonIgnoreProperties("participaciones")
    private EdicionFeria edicionFeria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_participante", nullable = false)
    @JsonIgnoreProperties("participaciones")
    private Participante participante;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30)
    private EstadoParticipacion estado;

    // ===================================================
    // CAMPOS DE COMPATIBILIDAD ANTERIOR (DEPRECATED)
    // ===================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feria_id")
    @JsonIgnoreProperties("participaciones")
    @Deprecated
    private Feria feria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stand_id")
    @JsonIgnoreProperties("participaciones")
    @Deprecated
    private Stand stand;

    @Deprecated
    private Integer numeroStand;

    @Deprecated
    private Double ventas;

    public Participacion() {
    }

    public Participacion(Feria feria, Stand stand, Integer numeroStand, EstadoParticipacion estado) {
        this.feria = feria;
        this.stand = stand;
        this.numeroStand = numeroStand;
        this.estado = estado;
        if (feria != null && feria.getTipoFeria() != null) {
            // Se puede intentar inferir la edición si fuese necesario
        }
    }

    public int getIdParticipacion() {
        return idParticipacion;
    }

    public void setIdParticipacion(int idParticipacion) {
        this.idParticipacion = idParticipacion;
    }

    // Método bridge de compatibilidad para getId()
    public int getId() {
        return idParticipacion;
    }

    public EdicionFeria getEdicionFeria() {
        return edicionFeria;
    }

    public void setEdicionFeria(EdicionFeria edicionFeria) {
        this.edicionFeria = edicionFeria;
        if (edicionFeria != null) {
            this.feria = edicionFeria.getFeria(); // Sincronización de compatibilidad
        }
    }

    public Participante getParticipante() {
        return participante;
    }

    public void setParticipante(Participante participante) {
        this.participante = participante;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public EstadoParticipacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoParticipacion estado) {
        this.estado = estado;
    }

    // ===================================================
    // GETTERS Y SETTERS DE COMPATIBILIDAD (DEPRECATED)
    // ===================================================
    @Deprecated
    public Feria getFeria() {
        return feria != null ? feria : (edicionFeria != null ? edicionFeria.getFeria() : null);
    }

    @Deprecated
    public void setFeria(Feria feria) {
        this.feria = feria;
    }

    @Deprecated
    public Stand getStand() {
        return stand;
    }

    @Deprecated
    public void setStand(Stand stand) {
        this.stand = stand;
    }

    @Deprecated
    public Integer getNumeroStand() {
        return numeroStand;
    }

    @Deprecated
    public void setNumeroStand(Integer numeroStand) {
        this.numeroStand = numeroStand;
    }

    @Deprecated
    public Double getVentas() {
        return ventas;
    }

    @Deprecated
    public void setVentas(Double ventas) {
        this.ventas = ventas;
    }
}
