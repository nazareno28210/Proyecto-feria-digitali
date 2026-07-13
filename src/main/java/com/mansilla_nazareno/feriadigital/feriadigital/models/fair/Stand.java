package com.mansilla_nazareno.feriadigital.feriadigital.models.fair;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Participante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.product.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.configurations.CloudinaryDefaults;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stand")
public class Stand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stand")
    private int idStand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_edicion_feria")
    private EdicionFeria edicionFeria;

    @Column(name = "codigo", length = 20)
    private String codigo; // Ej: A1, B2

    @Column(name = "metros_cuadrados", columnDefinition = "DECIMAL(5,2)")
    private double metrosCuadrados;

    @Column(name = "estado", length = 30)
    private String estadoStand = "DISPONIBLE"; // DISPONIBLE, RESERVADO, OCUPADO

    // ===================================================
    // CAMPOS DE COMPATIBILIDAD ANTERIOR (DEPRECATED)
    // ===================================================
    @Deprecated
    private String nombre;
    @Deprecated
    private String descripcion;
    @Deprecated
    private String imagenUrl;
    @Deprecated
    private String imagenPublicId;
    @Deprecated
    private boolean activo = true;
    @Deprecated
    private boolean javaEstado = true; // antiguo boolean estado

    public static final String IMAGEN_DEFAULT = CloudinaryDefaults.FERiante_DEFAULT_URL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feria_id")
    @Deprecated
    private Feria feria;

    @OneToMany(mappedBy = "stand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("stand")
    private List<Participacion> participaciones;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "feriante_id") // apunta a id_participante
    @JsonIgnoreProperties("stands")
    private Participante feriante;

    public Stand() {
    }

    public Stand(String nombre, String descripcion, String imagenUrl) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagenUrl = (imagenUrl == null || imagenUrl.isBlank()) ? IMAGEN_DEFAULT : imagenUrl;
    }

    public int getIdStand() {
        return idStand;
    }

    public void setIdStand(int idStand) {
        this.idStand = idStand;
    }

    // Método bridge de compatibilidad para getId()
    public int getId() {
        return idStand;
    }

    public void setId(int id) {
        this.idStand = id;
    }

    public EdicionFeria getEdicionFeria() {
        return edicionFeria;
    }

    public void setEdicionFeria(EdicionFeria edicionFeria) {
        this.edicionFeria = edicionFeria;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public double getMetrosCuadrados() {
        return metrosCuadrados;
    }

    public void setMetrosCuadrados(double metrosCuadrados) {
        this.metrosCuadrados = metrosCuadrados;
    }

    public String getEstadoStand() {
        return estadoStand;
    }

    public void setEstadoStand(String estadoStand) {
        this.estadoStand = estadoStand;
    }

    public List<Participacion> getParticipaciones() {
        return participaciones;
    }

    public void setParticipaciones(List<Participacion> participaciones) {
        this.participaciones = participaciones;
    }

    public Participante getFeriante() {
        return feriante;
    }

    public void setFeriante(Participante feriante) {
        this.feriante = feriante;
    }

    // ===================================================
    // MÉTODOS DE COMPATIBILIDAD ANTERIOR (DEPRECATED)
    // ===================================================
    @Deprecated
    public boolean isEstado() {
        return javaEstado;
    }

    @Deprecated
    public void setEstado(boolean estado) {
        this.javaEstado = estado;
    }

    @Deprecated
    public String getNombre() {
        return nombre;
    }

    @Deprecated
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Deprecated
    public String getDescripcion() {
        return descripcion;
    }

    @Deprecated
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Deprecated
    public Feria getFeria() {
        return feria;
    }

    @Deprecated
    public void setFeria(Feria feria) {
        this.feria = feria;
    }

    @Deprecated
    public String getImagenUrl() {
        return imagenUrl;
    }

    @Deprecated
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = (imagenUrl == null || imagenUrl.isBlank()) ? IMAGEN_DEFAULT : imagenUrl;
    }

    @Deprecated
    public String getImagenPublicId() {
        return imagenPublicId;
    }

    @Deprecated
    public void setImagenPublicId(String imagenPublicId) {
        this.imagenPublicId = imagenPublicId;
    }

    @Deprecated
    public boolean isActivo() {
        return activo;
    }

    @Deprecated
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Deprecated
    public List<Producto> getProductos() {
        return getFeriante() != null ? getFeriante().getProductos() : new ArrayList<>();
    }
}
