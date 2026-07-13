package com.mansilla_nazareno.feriadigital.feriadigital.models.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Persona;
import jakarta.persistence.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.product.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.feria.Stand;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participante")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_participante", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Participante {

    @OneToOne(mappedBy = "feriante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Stand stand;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participante")
    private int idParticipante;

    @Column(name = "tipo_participante", insertable = false, updatable = false)
    private String tipoParticipante;

    @Column(name = "nivel_registracion", length = 50)
    private String nivelRegistracion; // Ej: MONOTRIBUTO, RESPONSABLE_INSCRIPTO

    @Column(name = "estado_general", length = 30)
    private String estadoGeneral; // Ej: PENDIENTE, ACTIVO, INHABILITADO

    @Column(name = "fecha_de_alta")
    private LocalDateTime fechaDeAlta;

    @OneToMany(mappedBy = "participante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Producto> productos = new ArrayList<>();

    public Participante() {
    }

    public Participante(String nivelRegistracion, String estadoGeneral) {
        this.nivelRegistracion = nivelRegistracion;
        this.estadoGeneral = estadoGeneral;
        this.fechaDeAlta = LocalDateTime.now();
    }

    public int getIdParticipante() {
        return idParticipante;
    }

    public void setIdParticipante(int idParticipante) {
        this.idParticipante = idParticipante;
    }

    public String getTipoParticipante() {
        return tipoParticipante;
    }

    public String getNivelRegistracion() {
        return nivelRegistracion;
    }

    public void setNivelRegistracion(String nivelRegistracion) {
        this.nivelRegistracion = nivelRegistracion;
    }

    public String getEstadoGeneral() {
        return estadoGeneral;
    }

    public void setEstadoGeneral(String estadoGeneral) {
        this.estadoGeneral = estadoGeneral;
    }

    public LocalDateTime getFechaDeAlta() {
        return fechaDeAlta;
    }

    public void setFechaDeAlta(LocalDateTime fechaDeAlta) {
        this.fechaDeAlta = fechaDeAlta;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    // ===================================================
    // MÉTODOS DE COMPATIBILIDAD CON EL ANTIGUO FERIANTE
    // ===================================================
    public Usuario getUsuario() {
        if (this instanceof ParticipantePersona) {
            Persona p = ((ParticipantePersona) this).getPersona();
            return p != null ? p.getUsuario() : null;
        }
        return null;
    }

    public String getTelefono() {
        if (this instanceof ParticipantePersona) {
            Persona p = ((ParticipantePersona) this).getPersona();
            return p != null ? p.getTelefono() : null;
        }
        return null;
    }

    public Stand getStand() {
        return stand;
    }

    public void setStand(Stand stand) {
        this.stand = stand;
    }

    public int getId() {
        return getIdParticipante();
    }

    public LocalDate getFechaRegistro() {
        return getFechaDeAlta() != null ? getFechaDeAlta().toLocalDate() : null;
    }

    public com.mansilla_nazareno.feriadigital.feriadigital.models.auth.EstadoUsuario getUserEstate() {
        return getEstadoGeneral() != null && getEstadoGeneral().equalsIgnoreCase("ACTIVO")
                ? com.mansilla_nazareno.feriadigital.feriadigital.models.auth.EstadoUsuario.ACTIVO
                : com.mansilla_nazareno.feriadigital.feriadigital.models.auth.EstadoUsuario.INACTIVO;
    }
}
