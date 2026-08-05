package com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "votos_ferias")
public class VotoFeria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "feria_id", nullable = false)
    private Feria feria;

    @Column(nullable = false)
    private Boolean esPositivo;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public VotoFeria() {
    }

    public VotoFeria(Usuario usuario, Feria feria, Boolean esPositivo) {
        this.usuario = usuario;
        this.feria = feria;
        this.esPositivo = esPositivo;
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

    public Feria getFeria() {
        return feria;
    }

    public void setFeria(Feria feria) {
        this.feria = feria;
    }

    public Boolean getEsPositivo() {
        return esPositivo;
    }

    public void setEsPositivo(Boolean esPositivo) {
        this.esPositivo = esPositivo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
