package com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import jakarta.persistence.*;

import java.util.List;


@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;

    private String imagenUrl;
    private String imagenPublicId;
    public static final String IMAGEN_DEFAULT =
            "https://res.cloudinary.com/dklkf0fmq/image/upload/v1769030533/NOT_IMAGE_aypskv.png";

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private boolean eliminado = false; // 🟢 NUEVO: Para el borrado lógico

    @ManyToOne
    @JoinColumn(name = "stand_id")
    @JsonIgnoreProperties("productos")
    private Stand stand; // cada producto pertenece a un stand

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("productos")
    private List<CategoriaProducto> categorias; // un producto puede tener varias categorías

    @Enumerated(EnumType.STRING)
    private TipoVenta tipoVenta; //delimita la oferta
    private String unidadMedida; // "kg", "g", "m", "un", etc.


    private boolean estado;

    public Producto(){}
    public Producto(double precio, String descripcion, String nombre) {
        this.descripcion = descripcion;
        this.nombre = nombre;
        this.precio = precio;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setCategorias(List<CategoriaProducto> categorias) {
        this.categorias = categorias;
    }

    public void setStand(Stand stand) {
        this.stand = stand;
    }

    public Stand getStand() {
        return stand;
    }

    public List<CategoriaProducto> getCategorias() {
        return categorias;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public String getImagenPublicId() {
        return imagenPublicId;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl =
                (imagenUrl == null || imagenUrl.isBlank())
                        ? IMAGEN_DEFAULT
                        : imagenUrl;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public void setImagenPublicId(String imagenPublicId) {
        this.imagenPublicId = imagenPublicId;
    }

    public TipoVenta getTipoVenta() { return tipoVenta; }
    public void setTipoVenta(TipoVenta tipoVenta) { this.tipoVenta = tipoVenta; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
}
