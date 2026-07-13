package com.mansilla_nazareno.feriadigital.feriadigital.repositories.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Participante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {

    @Query("SELECT p FROM Participante p WHERE " +
           "(TYPE(p) = ParticipantePersona AND TREAT(p AS ParticipantePersona).persona.usuario = :usuario) OR " +
           "(TYPE(p) = ParticipanteEmpresa AND EXISTS (SELECT re FROM RepresentanteEmpresa re WHERE re.empresa = TREAT(p AS ParticipanteEmpresa).empresa AND re.persona.usuario = :usuario))")
    Optional<Participante> findByUsuario(@Param("usuario") Usuario usuario);
}
