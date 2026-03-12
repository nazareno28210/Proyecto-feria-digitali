package com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;

import jakarta.persistence.*;

@Entity
public class ImagenProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private String publicId;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    public ImagenProducto() {}

    public ImagenProducto(String url, String publicId, Producto producto) {
        this.url = url;
        this.publicId = publicId;
        this.producto = producto;
    }

    // Getters y Setters

    public String getUrl() { return url; }
    public String getPublicId() { return publicId; }

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }
}