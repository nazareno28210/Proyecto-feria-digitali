package com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;

import jakarta.persistence.*;

@Entity
@Table(name = "imagen_producto_compat")
@PrimaryKeyJoinColumn(name = "id_producto_imagen")
@Deprecated
public class ImagenProducto extends ProductoImagen {

    public ImagenProducto() {
    }

    public ImagenProducto(String url, String publicId, Producto producto) {
        super(producto, url, publicId, 0, false);
    }
}
