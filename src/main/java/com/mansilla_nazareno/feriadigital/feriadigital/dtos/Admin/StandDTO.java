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
        this.id = stand.getId();
        this.nombre = stand.getNombre();
        this.descripcion = stand.getDescripcion();
        this.imagenUrl = stand.getImagenUrl();

        // 🟢 FILTRO PARA EL PÚBLICO: Solo activos y NO eliminados
        this.productos = stand.getProductos()
                .stream()
                .filter(p -> p.isActivo() && !p.isEliminado())
                .map(ProductoDTO::new)
                .collect(Collectors.toList());

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
        this.id = stand.getId();
        this.nombre = stand.getNombre();
        this.descripcion = stand.getDescripcion();

        // 🟢 FILTRO PARA EL PÚBLICO: También aplicado aquí
        this.productos = stand.getProductos()
                .stream()
                .filter(p -> p.isActivo() && !p.isEliminado())
                .map(ProductoDTO::new)
                .collect(Collectors.toList());

        this.feriante = null; // Evita bucle infinito

        if (stand.getFeria() != null) {
            this.feriaId = stand.getFeria().getId();
        } else {
            this.feriaId = null;
        }
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public List<ProductoDTO> getProductos() { return productos; }
    public FerianteDTO getFeriante() { return feriante; }
    public String getImagenUrl() { return imagenUrl; }
    public Integer getFeriaId() { return feriaId; }
}