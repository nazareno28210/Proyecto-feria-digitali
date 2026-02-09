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
    private boolean activo; // 🟢 Se mantiene

    public StandDTO(Stand stand) {
        this.id = stand.getId();
        this.nombre = stand.getNombre();
        this.descripcion = stand.getDescripcion();
        this.imagenUrl = stand.getImagenUrl();
        this.activo = stand.isActivo();

        // 🟢 Lógica de protección centralizada
        this.productos = (stand.isActivo())
                ? stand.getProductos().stream()
                .filter(p -> p.isActivo() && !p.isEliminado())
                .map(ProductoDTO::new)
                .collect(Collectors.toList())
                : List.of(); // Lista vacía si está cerrado

        if (stand.getFeriante() != null) {
            this.feriante = new FerianteDTO(stand.getFeriante());
        }

        this.feriaId = (stand.getFeria() != null) ? stand.getFeria().getId() : null;
    }

    public StandDTO(Stand stand, boolean esParaFerianteDTO) {
        this.id = stand.getId();
        this.nombre = stand.getNombre();
        this.descripcion = stand.getDescripcion();
        this.activo = stand.isActivo();

        // 🟢 También protegemos aquí para ser consistentes
        this.productos = (stand.isActivo())
                ? stand.getProductos().stream()
                .filter(p -> p.isActivo() && !p.isEliminado())
                .map(ProductoDTO::new)
                .collect(Collectors.toList())
                : List.of();

        this.feriante = null;
        this.feriaId = (stand.getFeria() != null) ? stand.getFeria().getId() : null;
    }

    // Getters indispensables
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public List<ProductoDTO> getProductos() { return productos; }
    public FerianteDTO getFeriante() { return feriante; }
    public String getImagenUrl() { return imagenUrl; }
    public Integer getFeriaId() { return feriaId; }
    public boolean isActivo() { return activo; } // 🟢 AGREGADO: Importante para el JSON
}