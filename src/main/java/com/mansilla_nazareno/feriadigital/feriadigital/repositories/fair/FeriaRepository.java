package com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair;
import com.mansilla_nazareno.feriadigital.feriadigital.models.feria.Feria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeriaRepository extends JpaRepository<Feria, Integer> {

    // Para el Admin: Todas las ferias que no estén en la "papelera"
    List<Feria> findByEliminadoFalse();

    // Para el Público: Solo las activas y no eliminadas
    List<Feria> findByEstadoAndEliminadoFalse(String estado);


}

