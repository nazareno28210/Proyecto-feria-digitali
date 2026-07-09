package com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;

import jakarta.persistence.*;

@Entity
@Table(name = "producto_imagen")
@Inheritance(strategy = InheritanceType.JOINED)
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_imagen")
    private int idProductoImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "url", length = 255, nullable = false)
    private String url;

    @Column(name = "public_id")
    private String publicId; // Para Cloudinary

    @Column(name = "orden")
    private int orden;

    @Column(name = "principal")
    private boolean principal;

    public ProductoImagen() {
    }

    public ProductoImagen(Producto producto, String url, String publicId, int orden, boolean principal) {
        this.producto = producto;
        this.url = url;
        this.publicId = publicId;
        this.orden = orden;
        this.principal = principal;
    }

    public int getIdProductoImagen() {
        return idProductoImagen;
    }

    public Long getId() {
        return (long) idProductoImagen;
    }

    public void setIdProductoImagen(int idProductoImagen) {
        this.idProductoImagen = idProductoImagen;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }
}
