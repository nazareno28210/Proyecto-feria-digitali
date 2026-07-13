package com.mansilla_nazareno.feriadigital.feriadigital.repositories.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {
}
