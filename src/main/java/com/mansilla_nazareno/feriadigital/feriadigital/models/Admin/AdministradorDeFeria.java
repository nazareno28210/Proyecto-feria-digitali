package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import jakarta.persistence.*;

@Entity
@Table(name = "administrador_de_feria")
public class AdministradorDeFeria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    public AdministradorDeFeria() {}

    public int getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
