package com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.ResenaStand;
import java.time.LocalDateTime;

public class ResenaStandDTO {
    private Integer id;
    private Integer puntaje;
    private LocalDateTime fecha;
    private String nombreUsuario;
    private String fotoPerfil;

    public ResenaStandDTO() {
    }

    public ResenaStandDTO(ResenaStand resenaStand) {
        this.id = resenaStand.getId();
        this.puntaje = resenaStand.getPuntaje();
        this.fecha = resenaStand.getFechaCreacion();
        if (resenaStand.getUsuario() != null) {
            this.nombreUsuario = resenaStand.getUsuario().getNombre();
            this.fotoPerfil = resenaStand.getUsuario().getImagenUrl();
        }
    }

    public Integer getId() {
        return id;
    }

    public Integer getPuntaje() {
        return puntaje;
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
}
