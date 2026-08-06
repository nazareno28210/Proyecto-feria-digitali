package com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resenas_productos")
public class ResenaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer puntaje;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public ResenaProducto() {
    }

    public ResenaProducto(Usuario usuario, Producto producto, Integer puntaje, String comentario) {
        this.usuario = usuario;
        this.producto = producto;
        this.puntaje = puntaje;
        this.comentario = comentario;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Integer puntaje) {
        this.puntaje = puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }



    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


}
