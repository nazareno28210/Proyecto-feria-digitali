package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoEdicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EdicionFeriaRepository extends JpaRepository<EdicionFeria, Integer> {

    // Para buscar el historial completo de una determinada feria plantilla
    List<EdicionFeria> findByFeriaId(Integer feriaId);

    // Para filtrar las ediciones por estado
    List<EdicionFeria> findByEstado(EstadoEdicion estado);

    // Detecta solapamiento de fechas dentro de la misma feria (evalúa ediciones ACTIVAS o PROXIMAS)
    @Query("SELECT COUNT(e) > 0 FROM EdicionFeria e " +
           "WHERE e.feria.id = :feriaId " +
           "AND (:id IS NULL OR e.id != :id) " +
           "AND e.fechaInicio <= :fechaFin " +
           "AND e.fechaFinal >= :fechaInicio " +
           "AND (e.estado = com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoEdicion.ACTIVA OR e.estado = com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoEdicion.PROXIMA)")
    boolean existeSolapamiento(@Param("feriaId") Integer feriaId,
                               @Param("fechaInicio") LocalDate fechaInicio,
                               @Param("fechaFin") LocalDate fechaFin,
                               @Param("id") Integer id);
}
