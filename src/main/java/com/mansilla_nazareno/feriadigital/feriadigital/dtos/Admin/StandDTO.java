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
    private Integer feriaId;

    public StandDTO(Stand stand) {
        this.id=stand.getId();
        this.nombre =stand.getNombre();
        this.descripcion = stand.getDescripcion();
        this.imagenUrl = stand.getImagenUrl();
        this.productos =stand.getProductos()
                .stream()
                .map(ProductoDTO::new)
                .collect(Collectors.toList());
        this.feriante=getFeriante();
        if (stand.getFeriante() != null) {
            this.feriante = new FerianteDTO(stand.getFeriante());
        }
        if (stand.getFeria() != null) {
            this.feriaId = stand.getFeria().getId();
        } else {
            this.feriaId = null;
        }
    }
    public StandDTO(Stand stand, boolean esParaFerianteDTO) {
        this.id=stand.getId();
        this.nombre =stand.getNombre();
        this.descripcion = stand.getDescripcion();
        this.productos =stand.getProductos()
                .stream()
                .map(ProductoDTO::new)
                .collect(Collectors.toList());

        // ¡No inicializamos el feriante! Así evitamos el bucle infinito.
        this.feriante = null;
        if (stand.getFeria() != null) {
            this.feriaId = stand.getFeria().getId();
        } else {
            this.feriaId = null;
        }
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<ProductoDTO> getProductos() {
        return productos;
    }

    public FerianteDTO getFeriante() {
        return feriante;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }
    public Integer getFeriaId() {
        return feriaId;
    }

}
