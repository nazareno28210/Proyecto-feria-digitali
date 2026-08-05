package com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.ResenaProducto;
import java.time.LocalDateTime;

public class ResenaProductoDTO {
    private Integer id;
    private Integer puntaje;
    private String comentario;
    private LocalDateTime fecha;
    private String nombreUsuario;
    private String fotoPerfil;
    private String respuesta;
    private LocalDateTime fechaRespuesta;

    public ResenaProductoDTO() {
    }

    public ResenaProductoDTO(ResenaProducto resenaProducto) {
        this.id = resenaProducto.getId();
        this.puntaje = resenaProducto.getPuntaje();
        this.comentario = resenaProducto.getComentario();
        this.fecha = resenaProducto.getFechaCreacion();
        if (resenaProducto.getUsuario() != null) {
            this.nombreUsuario = resenaProducto.getUsuario().getNombre();
            this.fotoPerfil = resenaProducto.getUsuario().getImagenUrl();
        }
        this.respuesta = resenaProducto.getRespuesta();
        this.fechaRespuesta = resenaProducto.getFechaRespuesta();
    }

    public Integer getId() {
        return id;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }
}
