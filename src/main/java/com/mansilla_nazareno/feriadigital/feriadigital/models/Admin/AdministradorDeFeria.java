package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import jakarta.persistence.*;

@Entity
public class AdministradorDeFeria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "fk_id_usuario", referencedColumnName = "id")
    private Usuario usuario;


    public AdministradorDeFeria(){}


    public int getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (usuario.getTipoUsuario() == TipoUsuario.NORMAL) {
            usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        }
        this.usuario = usuario;
    }
}
