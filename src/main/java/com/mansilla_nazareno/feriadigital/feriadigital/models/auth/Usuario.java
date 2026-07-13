package com.mansilla_nazareno.feriadigital.feriadigital.models.auth;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.TipoUsuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int idUsuario;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @Column(name = "nombre_usuario", length = 50, nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "contraseña", length = 255, nullable = false)
    private String contrasena;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "usuario_rol",
        joinColumns = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private Set<Rol> roles = new HashSet<>();

    public Usuario() {
    }

    public Usuario(Persona persona, String nombreUsuario, String contrasena) {
        this.persona = persona;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.activo = false;
    }

    // ==========================================
    // CONSTRUCTOR DE COMPATIBILIDAD
    // ==========================================
    public Usuario(String nombre, String apellido, String email, String contrasena, EstadoUsuario estadoUsuario) {
        this.persona = new Persona(nombre, apellido, null, null, null);
        this.nombreUsuario = email;
        this.contrasena = contrasena;
        this.activo = (estadoUsuario == EstadoUsuario.ACTIVO);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }

    // ==========================================
    // MÉTODOS DE COMPATIBILIDAD (PUENTE)
    // ==========================================
    public int getId() {
        return idUsuario;
    }

    public String getEmail() {
        return nombreUsuario;
    }

    public void setEmail(String email) {
        this.nombreUsuario = email;
    }

    public String getNombre() {
        return persona != null ? persona.getNombre() : null;
    }

    public void setNombre(String nombre) {
        if (this.persona == null) {
            this.persona = new Persona();
        }
        this.persona.setNombre(nombre);
    }

    public String getApellido() {
        return persona != null ? persona.getApellido() : null;
    }

    public void setApellido(String apellido) {
        if (this.persona == null) {
            this.persona = new Persona();
        }
        this.persona.setApellido(apellido);
    }

    public String getImagenUrl() {
        return persona != null ? persona.getImagenUrl() : null;
    }

    public void setImagenUrl(String imagenUrl) {
        if (this.persona == null) {
            this.persona = new Persona();
        }
        this.persona.setImagenUrl(imagenUrl);
    }

    public String getImagenPublicId() {
        return persona != null ? persona.getImagenPublicId() : null;
    }

    public void setImagenPublicId(String imagenPublicId) {
        if (this.persona == null) {
            this.persona = new Persona();
        }
        this.persona.setImagenPublicId(imagenPublicId);
    }

    public boolean isEnabled() {
        return activo;
    }

    public void setEnabled(boolean enabled) {
        this.activo = enabled;
    }

    public void setUserEstate(EstadoUsuario estadoUsuario) {
        this.activo = (estadoUsuario == EstadoUsuario.ACTIVO);
    }

    public EstadoUsuario getEstadoUsuario() {
        return activo ? EstadoUsuario.ACTIVO : EstadoUsuario.INACTIVO;
    }

    public TipoUsuario getTipoUsuario() {
        if (roles.stream().anyMatch(r -> r.getNombre().equalsIgnoreCase("ADMINISTRADOR") || r.getNombre().equalsIgnoreCase("ADMIN"))) {
            return TipoUsuario.ADMINISTRADOR;
        }
        if (roles.stream().anyMatch(r -> r.getNombre().equalsIgnoreCase("FERIANTE"))) {
            return TipoUsuario.FERIANTE;
        }
        return TipoUsuario.NORMAL;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        if (tipoUsuario == null) return;
        String roleName = tipoUsuario == TipoUsuario.ADMINISTRADOR ? "ADMINISTRADOR" :
                          tipoUsuario == TipoUsuario.FERIANTE ? "FERIANTE" : "VISITANTE";
        this.roles.clear();
        this.roles.add(new Rol(roleName));
    }
}