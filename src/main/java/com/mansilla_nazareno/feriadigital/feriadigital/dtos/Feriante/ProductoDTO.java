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
}
