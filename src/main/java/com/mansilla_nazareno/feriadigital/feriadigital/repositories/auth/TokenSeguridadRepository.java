package com.mansilla_nazareno.feriadigital.feriadigital.repositories.auth;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.TokenSeguridad;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenSeguridadRepository extends JpaRepository<TokenSeguridad, Integer> {
    Optional<TokenSeguridad> findByToken(String token);
    Optional<TokenSeguridad> findByTokenAndTipoToken(String token, String tipoToken);
    void deleteByUsuario(Usuario usuario);
}
