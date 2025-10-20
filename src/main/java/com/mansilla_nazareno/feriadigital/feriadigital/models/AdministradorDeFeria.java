package com.mansilla_nazareno.feriadigital.feriadigital.models;

import jakarta.persistence.*;

@Entity
public class AdministradorDeFeria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "fk_id_usuario", referencedColumnName = "id")
    private Usuario usuario;

    private String telefono;

    public AdministradorDeFeria(){}
    public AdministradorDeFeria(String telefono){
        this.telefono=telefono;
    }

    public int getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        this.usuario = usuario;
    }
}
