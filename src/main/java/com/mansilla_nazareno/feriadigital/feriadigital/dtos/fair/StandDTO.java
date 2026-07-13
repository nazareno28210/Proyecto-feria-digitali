package com.mansilla_nazareno.feriadigital.feriadigital.dtos.fair;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.participant.ParticipanteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.product.ProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.fair.Stand;

import java.util.List;
import java.util.stream.Collectors;

public class StandDTO {
    private int id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private List<ProductoDTO> productos;
    private ParticipanteDTO feriante;
    private boolean activo;
    private Double promedioEstrellas;
    private int cantidadResenas;
    private List<Integer> feriasIds;
    private Integer usuarioDueñoId;

    // 1. Constructor Principal (Por defecto, ignora al feriante para evitar bucles)
    public StandDTO(Stand stand) {
        this(stand, true);
    }

    // 2. 🟢 CONSTRUCTOR ANTI-RECURSIVIDAD: Permite decidir si cargar o no al Feriante
    public StandDTO(Stand stand, boolean ignorarFeriante) {
        this.id = stand.getId();
        this.nombre = stand.getNombre();
        this.descripcion = stand.getDescripcion();
        this.imagenUrl = stand.getImagenUrl();
        this.activo = stand.isActivo();

        if (stand.getFeriante() != null && stand.getFeriante().getUsuario() != null) {
            this.usuarioDueñoId = stand.getFeriante().getUsuario().getId();
        } else {
            this.usuarioDueñoId = null;
        }

        // Si explícitamente pedimos NO ignorarlo, lo cargamos
        if (!ignorarFeriante && stand.getFeriante() != null) {
            // Se asume que FerianteDTO también tiene un constructor que ignora al Stand
            this.feriante = new ParticipanteDTO(stand.getFeriante(), true);
        } else {
            this.feriante = null;
        }

        this.productos = (stand.getProductos() != null)
                ? stand.getProductos().stream()
                .map(ProductoDTO::new)
                .collect(Collectors.toList())
                : List.of();

        this.feriasIds = (stand.getParticipaciones() != null)
                ? stand.getParticipaciones().stream()
                .map(p -> p.getFeria().getId())
                .collect(Collectors.toList())
                : List.of();
    }


    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public List<ProductoDTO> getProductos() { return productos; }
    public ParticipanteDTO getFeriante() { return feriante; }
    public String getImagenUrl() { return imagenUrl; }
    public boolean isActivo() { return activo; }
    public Double getPromedioEstrellas() { return promedioEstrellas; }
    public void setPromedioEstrellas(Double promedioEstrellas) { this.promedioEstrellas = promedioEstrellas; }
    public List<Integer> getFeriasIds() { return feriasIds; }
    public int getCantidadResenas() { return cantidadResenas; }
    public void setCantidadResenas(int cantidadResenas) { this.cantidadResenas = cantidadResenas; }
    public Integer getUsuarioDueñoId() { return usuarioDueñoId; }
}