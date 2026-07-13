package com.mansilla_nazareno.feriadigital.feriadigital.repositories.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.SolicitudParaFeriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SolicitudParaFerianteRepository extends JpaRepository<SolicitudParaFeriante, Integer> {
    Optional<SolicitudParaFeriante> findByUsuario(Usuario usuario);
}
