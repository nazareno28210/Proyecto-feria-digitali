package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdicionFeriaRepository extends JpaRepository<EdicionFeria, Integer> {

    // Para buscar el historial completo de una determinada feria plantilla
    List<EdicionFeria> findByFeriaId(Integer feriaId);

    // Para filtrar las ediciones activas que se mostrarán a los feriantes
    List<EdicionFeria> findByEstado(String estado);
}
