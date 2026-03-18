package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoParticipacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipacionRepository extends JpaRepository<Participacion, Integer> {

    List<Participacion> findByFeriaId(int feriaId);
    List<Participacion> findByFeria_IdAndEstado(int feriaId, EstadoParticipacion estado);

    List<Participacion> findByStandId(int standId);

    boolean existsByFeriaIdAndStandId(int feriaId, int standId);
}
