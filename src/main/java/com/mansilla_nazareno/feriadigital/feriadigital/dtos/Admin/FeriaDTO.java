package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class FeriaDTO {
    private int id;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private String lugar;
    private String descripcion;
    private String estado;
    private boolean eliminado;
    private String imagenUrl;
    private Integer porcentajeAprobacion;
    private Integer totalVotos;
    private Double latitud;
    private Double longitud;
    private Integer capacidad;

    private List<StandDTO> stands;

    public FeriaDTO(){}

    public FeriaDTO(Feria feria) {
        this.id = feria.getId();
        this.nombre = feria.getNombre();
        this.fechaInicio = feria.getFechaInicio();
        this.fechaFinal = feria.getFechaFinal();
        this.lugar = feria.getLugar();
        this.descripcion = feria.getDescripcion();
        this.estado = feria.getEstado();
        this.imagenUrl = feria.getImagenUrl();
        this.eliminado = feria.isEliminado();
        this.capacidad = feria.getCapacidad();

        // 🟢 LÓGICA DE TU AMIGO: Cargamos los stands a través de las participaciones
        this.stands = feria.getParticipaciones() != null
                ? feria.getParticipaciones().stream()
                .map(participacion -> new StandDTO(participacion.getStand()))
                .collect(Collectors.toList())
                : List.of();

        // 🟢 TU LÓGICA: Mantenemos la latitud y longitud para el mapa
        this.latitud = feria.getLatitud();
        this.longitud = feria.getLongitud();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFinal() {
        return fechaFinal;
    }

    public String getLugar() {
        return lugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public List<StandDTO> getStands() {
        return stands;
    }

    public void setStands(List<StandDTO> stands) {
        this.stands = stands;
    }

    public boolean isEliminado() {
        return eliminado;
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

    public Double getLongitud() {
        return longitud;
    }
    public Integer getCapacidad() { return capacidad; }

    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
}