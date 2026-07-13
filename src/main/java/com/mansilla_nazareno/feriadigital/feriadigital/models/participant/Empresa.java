package com.mansilla_nazareno.feriadigital.feriadigital.models.participant;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private int idEmpresa;

    @Column(name = "razon_social", length = 150, nullable = false)
    private String razonSocial;

    @Column(name = "nombre_fantasia", length = 150)
    private String nombreFantasia;

    @Column(name = "cuit", length = 20, nullable = false, unique = true)
    private String cuit;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "fecha_de_alta", nullable = false)
    private LocalDateTime fechaDeAlta;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Empresa() {
    }

    public Empresa(String razonSocial, String nombreFantasia, String cuit, String email, String telefono, String direccion) {
        this.razonSocial = razonSocial;
        this.nombreFantasia = nombreFantasia;
        this.cuit = cuit;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fechaDeAlta = LocalDateTime.now();
        this.activo = true;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreFantasia() {
        return nombreFantasia;
    }

    public void setNombreFantasia(String nombreFantasia) {
        this.nombreFantasia = nombreFantasia;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDateTime getFechaDeAlta() {
        return fechaDeAlta;
    }

    public void setFechaDeAlta(LocalDateTime fechaDeAlta) {
        this.fechaDeAlta = fechaDeAlta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
