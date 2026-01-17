package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeriaRepository extends JpaRepository<Feria, Integer> {

    List<Feria> findByEstado(String estado);
}
