package com.mansilla_nazareno.feriadigital.feriadigital.models.feria;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asignacion_stand")
public class AsignacionStand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private int idAsignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_participacion", nullable = false)
    private Participacion participacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_stand", nullable = false)
    private Stand stand;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "costo_participacion", columnDefinition = "DECIMAL(10,2)", nullable = false)
    private double costoParticipacion;

    public AsignacionStand() {
    }

    public AsignacionStand(Participacion participacion, Stand stand, LocalDateTime fechaAsignacion, double costoParticipacion) {
        this.participacion = participacion;
        this.stand = stand;
        this.fechaAsignacion = fechaAsignacion;
        this.costoParticipacion = costoParticipacion;
    }

    public int getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(int idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    // Método bridge de compatibilidad para getId()
    public int getId() {
        return idAsignacion;
    }

    public Participacion getParticipacion() {
        return participacion;
    }

    public void setParticipacion(Participacion participacion) {
        this.participacion = participacion;
    }

    public Stand getStand() {
        return stand;
    }

    public void setStand(Stand stand) {
        this.stand = stand;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public double getCostoParticipacion() {
        return costoParticipacion;
    }

    public void setCostoParticipacion(double costoParticipacion) {
        this.costoParticipacion = costoParticipacion;
    }
}
