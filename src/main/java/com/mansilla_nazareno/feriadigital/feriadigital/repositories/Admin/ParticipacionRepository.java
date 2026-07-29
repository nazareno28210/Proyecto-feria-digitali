package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipacionRepository extends JpaRepository<Participacion, Integer> {

    // Obtener todas las postulaciones asociadas a una edición específica
    List<Participacion> findByEdicionId(int edicionId);

    // Filtrar postulaciones por edición y estado de logística (ej: CONFIRMADO)
    List<Participacion> findByEdicionIdAndEstado(int edicionId, EstadoParticipacion estado);

    // Buscar una postulación específica para ver si el stand ya se anotó en esa edición
    Optional<Participacion> findByEdicionIdAndStandId(int edicionId, int standId);


    // Busca si un stand ya está registrado en una edición específica
    boolean existsByEdicionIdAndStandId(int edicionId, int standId);

    // Busca el primer participante en determinado estado ordenado por llegada (ID)
    Optional<Participacion> findFirstByEdicionIdAndEstadoOrderByIdAsc(Integer edicionId, EstadoParticipacion estado);

    // 🌟 HISTORIAL: Trae todas las ferias a las que asistió este stand en la historia del sistema
    List<Participacion> findByStandId(int standId);
}