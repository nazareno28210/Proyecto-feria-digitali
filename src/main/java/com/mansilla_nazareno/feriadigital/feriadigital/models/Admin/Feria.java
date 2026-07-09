package com.mansilla_nazareno.feriadigital.feriadigital.models.Admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "feria")
public class Feria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feria")
    private int idFeria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_feria")
    private TipoDeFeria tipoFeria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_organizador")
    private Usuario usuarioOrganizador;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private boolean eliminado = false;

    // ===================================================
    // CAMPOS DE COMPATIBILIDAD ANTERIOR (DEPRECATED)
    // ===================================================
    @Deprecated
    private LocalDate fechaInicio;
    @Deprecated
    private LocalDate fechaFinal;
    @Deprecated
    private String lugar;
    @Deprecated
    private String estado;
    @Deprecated
    private String imagenUrl;
    @Deprecated
    private Double latitud;
    @Deprecated
    private Double longitud;

    @OneToMany(mappedBy = "feria", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("feria")
    private List<Participacion> participaciones;

    public Feria() {
    }

    public Feria(String nombre, LocalDate fechaInicio, LocalDate fechaFinal, String lugar, String descripcion, String estado, String imagenUrl, Double latitud, Double longitud) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
        this.lugar = lugar;
        this.descripcion = descripcion;
        this.estado = estado;
        this.imagenUrl = imagenUrl;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public int getIdFeria() {
        return idFeria;
    }

    public void setIdFeria(int idFeria) {
        this.idFeria = idFeria;
    }

    // Método bridge de compatibilidad para getId()
    public int getId() {
        return idFeria;
    }

    public TipoDeFeria getTipoFeria() {
        return tipoFeria;
    }

    public void setTipoFeria(TipoDeFeria tipoFeria) {
        this.tipoFeria = tipoFeria;
    }

    public Usuario getUsuarioOrganizador() {
        return usuarioOrganizador;
    }

    public void setUsuarioOrganizador(Usuario usuarioOrganizador) {
        this.usuarioOrganizador = usuarioOrganizador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public List<Participacion> getParticipaciones() {
        return participaciones;
    }

    public void setParticipaciones(List<Participacion> participaciones) {
        this.participaciones = participaciones;
    }

    // ===================================================
    // GETTERS Y SETTERS DE COMPATIBILIDAD (DEPRECATED)
    // ===================================================
    @Deprecated
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    @Deprecated
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    @Deprecated
    public LocalDate getFechaFinal() {
        return fechaFinal;
    }

    @Deprecated
    public void setFechaFinal(LocalDate fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    @Deprecated
    public String getLugar() {
        return lugar;
    }

    @Deprecated
    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    @Deprecated
    public String getEstado() {
        return estado;
    }

    @Deprecated
    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Deprecated
    public String getImagenUrl() {
        return imagenUrl;
    }

    @Deprecated
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    @Deprecated
    public Double getLatitud() {
        return latitud;
    }

    @Deprecated
    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    @Deprecated
    public Double getLongitud() {
        return longitud;
    }

    @Deprecated
    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }
}
