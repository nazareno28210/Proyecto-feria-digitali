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
    private List<CategoriaProductoDTO> categorias;
    private String imagenUrl;
    private TipoVenta tipoVenta;
    private String unidadMedida;


    public ProductoDTO(Producto producto) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.activo = producto.isActivo();
        this.imagenUrl = producto.getImagenUrl();
        this.categorias = producto.getCategorias() == null
                ? List.of()
                : producto.getCategorias()
                .stream()
                .map(CategoriaProductoDTO::new)
                .collect(Collectors.toList());;
        this.tipoVenta = producto.getTipoVenta();
        this.unidadMedida = producto.getUnidadMedida();
    }

    // getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public boolean isActivo() { return activo; }
    public String getImagenUrl() {return imagenUrl;}
    public List<CategoriaProductoDTO> getCategorias() { return categorias; }
    public TipoVenta getTipoVenta() {return tipoVenta;}
    public String getUnidadMedida() {return unidadMedida;}


}
