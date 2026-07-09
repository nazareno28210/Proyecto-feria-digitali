package com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {
}
