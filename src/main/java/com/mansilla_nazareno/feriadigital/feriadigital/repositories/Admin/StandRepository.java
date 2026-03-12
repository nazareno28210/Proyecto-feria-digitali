package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoParticipacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StandRepository extends JpaRepository<Stand,Integer> {

    Stand findByFeriante(Feriante feriante);

    // 🟢 Para el público: Solo traer los que el feriante tenga "abiertos"
    List<Stand> findByActivoTrue();
    // Ejemplo para obtener solo stands confirmados en una feria específica
    List<Participacion> findByFeriaIdAndEstado(int feriaId, EstadoParticipacion estado);
}
