package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.FerianteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.ProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;

import java.util.List;
import java.util.stream.Collectors;

public class StandDTO {
    private int id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private List<ProductoDTO> productos;
    private FerianteDTO feriante;
    private boolean activo;
    private Double promedioEstrellas;
    private int cantidadResenas;
    private List<Integer> feriasIds;

    // Agregar a StandDTO.java
    public StandDTO(Stand stand) {
        this.id = stand.getId();
        this.nombre = stand.getNombre();
        this.descripcion = stand.getDescripcion();
        this.imagenUrl = stand.getImagenUrl();
        this.activo = stand.isActivo();
        this.feriante = null;
        // 1. SOLUCIÓN AL CONFLICTO DE PRODUCTOS:
        this.productos = (stand.getProductos() != null)
                ? stand.getProductos().stream()
                .map(ProductoDTO::new)
                .collect(Collectors.toList())
                : List.of();

        // 2. MAPEO DE FERIAS:
        this.feriasIds = (stand.getParticipaciones() != null)
                ? stand.getParticipaciones().stream()
                .map(p -> p.getFeria().getId())
                .collect(Collectors.toList())
                : List.of();
    }

    // Actualizar el getter correspondiente


    // Getters indispensables

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public List<ProductoDTO> getProductos() { return productos; }
    public FerianteDTO getFeriante() { return feriante; }
    public String getImagenUrl() { return imagenUrl; }
    public boolean isActivo() { return activo; } // 🟢 AGREGADO: Importante para el JSON
    public Double getPromedioEstrellas() { return promedioEstrellas; }
    public void setPromedioEstrellas(Double promedioEstrellas) { this.promedioEstrellas = promedioEstrellas; }
    public List<Integer> getFeriasIds() { return feriasIds; }
    public int getCantidadResenas() { return cantidadResenas; }
    public void setCantidadResenas(int cantidadResenas) { this.cantidadResenas = cantidadResenas; }}
