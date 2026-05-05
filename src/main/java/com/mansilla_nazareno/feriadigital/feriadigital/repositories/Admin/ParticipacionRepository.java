package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipacionRepository extends JpaRepository<Participacion, Integer> {

    List<Participacion> findByFeriaId(int feriaId);
    List<Participacion> findByFeria_IdAndEstado(int feriaId, EstadoParticipacion estado);
    // 🟢 AGREGÁ ESTE: Para buscar la fila específica y ver si está CANCELADA
    Optional<Participacion> findByFeriaIdAndStandId(int feriaId, int standId);
    // Busca si existe otra participación con el mismo número de mesa en la misma feria
    boolean existsByFeriaIdAndNumeroStandAndIdNot(Integer feriaId, Integer numeroStand, Integer id);
    List<Participacion> findByStandId(int standId);
    boolean existsByFeriaIdAndStandId(int feriaId, int standId);
}
