package com.mansilla_nazareno.feriadigital.feriadigital.repositories;

import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoToken;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioTokenRepository extends JpaRepository<UsuarioToken, Long> {

    Optional<UsuarioToken> findByToken(String token);

    Optional<UsuarioToken> findByTokenAndTipoToken(String token, TipoToken tipoToken);

    List<UsuarioToken> findByUsuarioAndTipoTokenAndUsadoFalse(Usuario usuario, TipoToken tipoToken);

    void deleteByUsuarioAndTipoToken(Usuario usuario, TipoToken tipoToken);
}
