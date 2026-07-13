package com.mansilla_nazareno.feriadigital.feriadigital.models.fair;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "edicion_feria")
public class EdicionFeria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_edicion_feria")
    private int idEdicionFeria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_feria", nullable = false)
    private Feria feria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion", nullable = false)
    private Ubicacion ubicacion;

    @Column(name = "numero_edicion", length = 50, nullable = false)
    private String numeroEdicion; // Ej: Edición Invierno 2026

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "apertura_inscripcion")
    private LocalDateTime aperturaInscripcion;

    @Column(name = "cierre_inscripcion")
    private LocalDateTime cierreInscripcion;

    @Column(name = "capacidad_max")
    private int capacidadMax;

    @Column(name = "expediente_municipal", length = 50)
    private String expedienteMunicipal;

    @Column(name = "seguro_url", length = 255)
    private String seguroUrl;

    @Column(name = "estado", length = 50, nullable = false)
    private String estado; // PENDIENTE_LEGAL, ABIERTA, EN_CURSO, FINALIZADA

    public EdicionFeria() {
    }

    public EdicionFeria(Feria feria, Ubicacion ubicacion, String numeroEdicion, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.feria = feria;
        this.ubicacion = ubicacion;
        this.numeroEdicion = numeroEdicion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getIdEdicionFeria() {
        return idEdicionFeria;
    }

    public void setIdEdicionFeria(int idEdicionFeria) {
        this.idEdicionFeria = idEdicionFeria;
    }

    // Método bridge de compatibilidad para getId()
    public int getId() {
        return idEdicionFeria;
    }

    public Feria getFeria() {
        return feria;
    }

    public void setFeria(Feria feria) {
        this.feria = feria;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getNumeroEdicion() {
        return numeroEdicion;
    }

    public void setNumeroEdicion(String numeroEdicion) {
        this.numeroEdicion = numeroEdicion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getAperturaInscripcion() {
        return aperturaInscripcion;
    }

    public void setAperturaInscripcion(LocalDateTime aperturaInscripcion) {
        this.aperturaInscripcion = aperturaInscripcion;
    }

    public LocalDateTime getCierreInscripcion() {
        return cierreInscripcion;
    }

    public void setCierreInscripcion(LocalDateTime cierreInscripcion) {
        this.cierreInscripcion = cierreInscripcion;
    }

    public int getCapacidadMax() {
        return capacidadMax;
    }

    public void setCapacidadMax(int capacidadMax) {
        this.capacidadMax = capacidadMax;
    }

    public String getExpedienteMunicipal() {
        return expedienteMunicipal;
    }

    public void setExpedienteMunicipal(String expedienteMunicipal) {
        this.expedienteMunicipal = expedienteMunicipal;
    }

    public String getSeguroUrl() {
        return seguroUrl;
    }

    public void setSeguroUrl(String seguroUrl) {
        this.seguroUrl = seguroUrl;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
