package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ediciones_feria")

public class EdicionFeria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "feria_id", nullable = false)
    private Feria feria; // Relación con la plantilla base

    @Column(name = "nombre_edicion", nullable = false)
    private String nombreEdicion; // Ej: "Invierno 2026"

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_final", nullable = false)
    private LocalDate fechaFinal;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEdicion estado = EstadoEdicion.PROXIMA;

    @Column(name = "mapa_url")
    private String mapaUrl;

    @Column(name = "mapa_public_id")
    private String mapaPublicId;

    @Column(name = "capacidad")
    private Integer capacidad;

    @OneToMany(mappedBy = "edicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Espacio> espacios = new ArrayList<>();

    // Constructores
    public EdicionFeria() {}

    public EdicionFeria(Feria feria, String nombreEdicion, LocalDate fechaInicio, LocalDate fechaFinal, EstadoEdicion estado) {
        this.feria = feria;
        this.nombreEdicion = nombreEdicion;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
        this.estado = estado != null ? estado : EstadoEdicion.PROXIMA;
    }

    public EdicionFeria(Feria feria, String nombreEdicion, LocalDate fechaInicio, LocalDate fechaFinal, String estado) {
        this.feria = feria;
        this.nombreEdicion = nombreEdicion;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
        if (estado != null) {
            try {
                this.estado = EstadoEdicion.valueOf(estado.toUpperCase());
            } catch (Exception e) {
                this.estado = EstadoEdicion.PROXIMA;
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Feria getFeria() {
        return feria;
    }

    public void setFeria(Feria feria) {
        this.feria = feria;
    }

    public String getNombreEdicion() {
        return nombreEdicion;
    }

    public void setNombreEdicion(String nombreEdicion) {
        this.nombreEdicion = nombreEdicion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(LocalDate fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    // Y sus getters y setters correspondientes:
    public LocalTime getHoraInicio() { return horaInicio; }

    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }

    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public EstadoEdicion getEstado() {
        return estado;
    }

    public void setEstado(EstadoEdicion estado) {
        this.estado = estado;
    }

    public void setEstado(String estado) {
        if (estado != null) {
            try {
                this.estado = EstadoEdicion.valueOf(estado.toUpperCase());
            } catch (Exception e) {
                // Ignore or leave current
            }
        }
    }

    public List<Espacio> getEspacios() { return espacios; }
    public void setEspacios(List<Espacio> espacios) { this.espacios = espacios; }

    public String getMapaUrl() { return mapaUrl; }
    public void setMapaUrl(String mapaUrl) { this.mapaUrl = mapaUrl; }

    public String getMapaPublicId() { return mapaPublicId; }
    public void setMapaPublicId(String mapaPublicId) { this.mapaPublicId = mapaPublicId; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
}
