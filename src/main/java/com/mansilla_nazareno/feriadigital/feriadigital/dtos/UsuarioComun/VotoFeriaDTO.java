package com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.VotoFeria;
import java.time.LocalDateTime;

public class VotoFeriaDTO {
    private Integer id;
    private Boolean esPositivo;
    private LocalDateTime fecha;
    private String nombreUsuario;

    public VotoFeriaDTO() {
    }

    public VotoFeriaDTO(VotoFeria votoFeria) {
        this.id = votoFeria.getId();
        this.esPositivo = votoFeria.getEsPositivo();
        this.fecha = votoFeria.getFechaCreacion();
        if (votoFeria.getUsuario() != null) {
            this.nombreUsuario = votoFeria.getUsuario().getNombre();
        }
    }

    public Integer getId() {
        return id;
    }

    public Boolean getEsPositivo() {
        return esPositivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}
