package com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair;
import com.mansilla_nazareno.feriadigital.feriadigital.models.fair.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.fair.EstadoParticipacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipacionRepository extends JpaRepository<Participacion, Integer> {

    List<Participacion> findByFeriaId(int feriaId);
    List<Participacion> findByFeria_IdAndEstado(int feriaId, EstadoParticipacion estado);

    List<Participacion> findByStandId(int standId);

    boolean existsByFeriaIdAndStandId(int feriaId, int standId);
}
