package com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Resena;

import java.time.LocalDateTime;

public class ResenaDTO {
    private Integer id;
    private Integer puntaje;
    private String comentario;
    private LocalDateTime fecha;
    private String nombreUsuario; // Solo el nombre para mostrar en la web

    public ResenaDTO(Resena resena) {
        this.id = resena.getId();
        this.puntaje = resena.getPuntaje();
        this.comentario = resena.getComentario();
        this.fecha = resena.getFechaCreacion();
        this.nombreUsuario = resena.getUsuario().getNombre(); // Asumiendo que Usuario tiene getNombre()
    }

    // Getters
    public Integer getId() { return id; }
    public Integer getPuntaje() { return puntaje; }
    public String getComentario() { return comentario; }
    public LocalDateTime getFecha() { return fecha; }
    public String getNombreUsuario() { return nombreUsuario; }
}
