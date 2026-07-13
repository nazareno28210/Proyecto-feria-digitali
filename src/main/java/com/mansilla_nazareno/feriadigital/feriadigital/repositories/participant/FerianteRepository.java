package com.mansilla_nazareno.feriadigital.feriadigital.repositories.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Persona;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Deprecated
public interface FerianteRepository extends JpaRepository<Feriante, Integer> {

    @Query("SELECT f FROM Feriante f WHERE f.persona.usuario = :usuario")
    Feriante findByUsuario(@Param("usuario") Usuario usuario);
}
