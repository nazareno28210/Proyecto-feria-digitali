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
<<<<<<< HEAD
    private String categoriaNombre;
    private String imagenUrl; // NUEVO
=======
    private List<CategoriaProductoDTO> categorias;
    private String imagenUrl;
    private TipoVenta tipoVenta;
    private String unidadMedida;

>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e

    public ProductoDTO(Producto producto) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.activo = producto.isActivo();
        this.imagenUrl = producto.getImagenUrl();
<<<<<<< HEAD
        if (producto.getCategoria() != null) {
            this.categoriaNombre = producto.getCategoria().getNombre();
        } else {
            this.categoriaNombre = "Sin categoría";
        }
=======
        this.categorias = producto.getCategorias() == null
                ? List.of()
                : producto.getCategorias()
                .stream()
                .map(CategoriaProductoDTO::new)
                .collect(Collectors.toList());;
        this.tipoVenta = producto.getTipoVenta();
        this.unidadMedida = producto.getUnidadMedida();
>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e
    }

    // getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public boolean isActivo() { return activo; }
    public String getImagenUrl() {return imagenUrl;}
<<<<<<< HEAD
    public String getCategoriaNombre() {
        return categoriaNombre;
    }
=======
    public List<CategoriaProductoDTO> getCategorias() { return categorias; }
    public TipoVenta getTipoVenta() {return tipoVenta;}
    public String getUnidadMedida() {return unidadMedida;}


>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e
}
