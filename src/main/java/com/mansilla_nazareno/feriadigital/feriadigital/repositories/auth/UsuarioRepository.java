package com.mansilla_nazareno.feriadigital.feriadigital.repositories.auth;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Usuario findByNombreUsuario(String nombreUsuario);

    // Alias temporal para mantener compatibilidad con controladores no migrados
    default Usuario findByEmail(String email) {
        return findByNombreUsuario(email);
    }
}
