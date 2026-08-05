package com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resenas_stands")
public class ResenaStand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "stand_id", nullable = false)
    private Stand stand;

    @Column(nullable = false)
    private Integer puntaje;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public ResenaStand() {
    }

    public ResenaStand(Usuario usuario, Stand stand, Integer puntaje) {
        this.usuario = usuario;
        this.stand = stand;
        this.puntaje = puntaje;
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

    public Stand getStand() {
        return stand;
    }

    public void setStand(Stand stand) {
        this.stand = stand;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Integer puntaje) {
        this.puntaje = puntaje;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
