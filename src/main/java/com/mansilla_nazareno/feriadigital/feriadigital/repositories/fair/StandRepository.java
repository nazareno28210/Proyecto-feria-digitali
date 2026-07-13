package com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair;
import com.mansilla_nazareno.feriadigital.feriadigital.models.feria.EstadoParticipacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.feria.Stand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StandRepository extends JpaRepository<Stand,Integer> {

    Stand findByFeriante(Feriante feriante);

    // 🟢 CORRECTO: Busca los stands navegando a través de las participaciones
    List<Stand> findDistinctByParticipaciones_Feria_IdAndParticipaciones_Estado(
            int feriaId,
            EstadoParticipacion estado
    );

}