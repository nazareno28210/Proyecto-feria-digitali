package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeriaRepository extends JpaRepository<Feria, Integer> {

    // Para el Admin: Todas las ferias plantilla que no estén en la "papelera"
    List<Feria> findByEliminadoFalse();
}