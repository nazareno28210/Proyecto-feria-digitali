package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import java.time.LocalDate;
import java.time.LocalTime;

public class EdicionFeriaDTO {

    private Integer id;
    private Integer feriaId;
    private String nombreEdicion;
    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;

    // 🟢 CAMPOS DE LA FERIA BASE (EL MOLDE)
    private String feriaNombre;
    private String feriaLugar;
    private String feriaDescripcion;
    private String feriaImagenUrl;
    private Double latitud;
    private Double longitud;

    public EdicionFeriaDTO() {}

    public EdicionFeriaDTO(EdicionFeria edicion) {
        this.id = edicion.getId();
        this.nombreEdicion = edicion.getNombreEdicion();
        this.fechaInicio = edicion.getFechaInicio();
        this.fechaFinal = edicion.getFechaFinal();
        this.horaInicio = edicion.getHoraInicio();
        this.horaFin = edicion.getHoraFin();
        this.estado = edicion.getEstado();

        // 🟢 Extraemos los datos de la feria base si existe
        if (edicion.getFeria() != null) {
            this.feriaId = edicion.getFeria().getId();
            this.feriaNombre = edicion.getFeria().getNombre();
            this.feriaLugar = edicion.getFeria().getLugar();
            this.feriaDescripcion = edicion.getFeria().getDescripcion();
            this.feriaImagenUrl = edicion.getFeria().getImagenUrl();
            this.latitud = edicion.getFeria().getLatitud();
            this.longitud = edicion.getFeria().getLongitud();
        }
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getFeriaId() { return feriaId; }
    public void setFeriaId(Integer feriaId) { this.feriaId = feriaId; }
    public String getNombreEdicion() { return nombreEdicion; }
    public void setNombreEdicion(String nombreEdicion) { this.nombreEdicion = nombreEdicion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFinal() { return fechaFinal; }
    public void setFechaFinal(LocalDate fechaFinal) { this.fechaFinal = fechaFinal; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFeriaNombre() { return feriaNombre; }
    public void setFeriaNombre(String feriaNombre) { this.feriaNombre = feriaNombre; }
    public String getFeriaLugar() { return feriaLugar; }
    public void setFeriaLugar(String feriaLugar) { this.feriaLugar = feriaLugar; }
    public String getFeriaDescripcion() { return feriaDescripcion; }
    public void setFeriaDescripcion(String feriaDescripcion) { this.feriaDescripcion = feriaDescripcion; }
    public String getFeriaImagenUrl() { return feriaImagenUrl; }
    public void setFeriaImagenUrl(String feriaImagenUrl) { this.feriaImagenUrl = feriaImagenUrl; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}