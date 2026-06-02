package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;

public class FeriaDTO {
    private int id;
    private String nombre;
    private String lugar;
    private String descripcion;
    private boolean eliminado;
    private String imagenUrl;
    private Integer porcentajeAprobacion;
    private Integer totalVotos;
    private Double latitud;
    private Double longitud;
    private Integer capacidad;

    public FeriaDTO(){}

    public FeriaDTO(Feria feria) {
        this.id = feria.getId();
        this.nombre = feria.getNombre();
        this.lugar = feria.getLugar();
        this.descripcion = feria.getDescripcion();
        this.imagenUrl = feria.getImagenUrl();
        this.eliminado = feria.isEliminado();
        this.capacidad = feria.getCapacidad();
        this.latitud = feria.getLatitud();
        this.longitud = feria.getLongitud();
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public void setPorcentajeAprobacion(Integer porcentajeAprobacion) {
        this.porcentajeAprobacion = porcentajeAprobacion;
    }

    public void setTotalVotos(Integer totalVotos) {
        this.totalVotos = totalVotos;
    }

    public Integer getPorcentajeAprobacion() {
        return porcentajeAprobacion;
    }

    public Integer getTotalVotos() {
        return totalVotos;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }
}