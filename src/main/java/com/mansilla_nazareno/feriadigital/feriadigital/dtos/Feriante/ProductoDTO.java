package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.TipoVenta;

import java.util.List;
import java.util.stream.Collectors;

public class ProductoDTO {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private boolean activo;
    private String categoriaNombre;
    private String imagenUrl; // NUEVO
    private int categoriaId; // 🟢 Agregamos el ID para el frontend
    private String tipoVenta; // 🟢 String para evitar problemas de mapeo en JS
    private String unidadMedida;
    private String feriaNombre;
    private String standNombre; // 🟢 PASO 1: Agregar el campo
    private int usuarioDueñoId; // ID del Usuario que es dueño del stand
    private Double promedioEstrellas;
    private int cantidadResenas;


    public ProductoDTO(Producto producto) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.activo = producto.isActivo();
        this.imagenUrl = producto.getImagenUrl();

        // Seteamos el tipo de venta y unidad [cite: 95, 97]
        if (producto.getTipoVenta() != null) {
            this.tipoVenta = producto.getTipoVenta().name();
        }
        this.unidadMedida = producto.getUnidadMedida();

        // Seteamos los datos de categoría [cite: 71, 82]
        if (producto.getCategoria() != null) {
            this.categoriaNombre = producto.getCategoria().getNombre();
            this.categoriaId = producto.getCategoria().getId();
        } else {
            this.categoriaNombre = "Sin categoría";
            this.categoriaId = 0;
        }

        // 🟢 Obtenemos el nombre de la feria navegando: Producto -> Stand -> Participaciones -> Feria
        if (producto.getStand() != null &&
                producto.getStand().getParticipaciones() != null &&
                !producto.getStand().getParticipaciones().isEmpty()) {

            // Obtenemos la feria desde la primera participación
            this.feriaNombre = producto.getStand().getParticipaciones().get(0).getFeria().getNombre();
        } else {
            this.feriaNombre = "Feria General";
        }

        // 🟢 PASO 2: Mapear el nombre del Stand
        if (producto.getStand() != null) {
            this.standNombre = producto.getStand().getNombre();
        } else {
            this.standNombre = "Stand General";
        }

        if (producto.getStand() != null && producto.getStand().getFeriante() != null) {
            this.usuarioDueñoId = producto.getStand().getFeriante().getUsuario().getId();
        }
    }

    // Getters necesarios
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public boolean isActivo() { return activo; }
    public String getImagenUrl() { return imagenUrl; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public int getCategoriaId() { return categoriaId; }
    public String getTipoVenta() { return tipoVenta; }
    public String getUnidadMedida() { return unidadMedida; }
    public String getFeriaNombre() { return feriaNombre; }
    public String getStandNombre() { return standNombre; }
    public int getUsuarioDueñoId() { return usuarioDueñoId; }

    public int getCantidadResenas() {
        return cantidadResenas;
    }

    public Double getPromedioEstrellas() {
        return promedioEstrellas;
    }

    public void setCantidadResenas(int cantidadResenas) {
        this.cantidadResenas = cantidadResenas;
    }

    public void setPromedioEstrellas(Double promedioEstrellas) {
        this.promedioEstrellas = promedioEstrellas;
    }
}
