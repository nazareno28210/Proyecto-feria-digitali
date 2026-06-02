package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StandRepository extends JpaRepository<Stand, Integer> {

    Stand findByFeriante(Feriante feriante);

    // 🟢 CORREGIDO: Ahora navega de forma segura a través de la edición cronológica (edicion_id)
    List<Stand> findDistinctByParticipaciones_Edicion_IdAndParticipaciones_Estado(
            int edicionId,
            EstadoParticipacion estado
    );
}