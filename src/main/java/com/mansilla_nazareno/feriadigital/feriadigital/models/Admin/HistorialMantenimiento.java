package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_mantenimientos")
public class HistorialMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "stand_id", nullable = false)
    private Espacio espacio;

    @Column(nullable = false)
    private String motivo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    public HistorialMantenimiento() {}

    public HistorialMantenimiento(Espacio espacio, String motivo, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.espacio = espacio;
        this.motivo = motivo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Espacio getEspacio() { return espacio; }
    public void setEspacio(Espacio espacio) { this.espacio = espacio; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
}
