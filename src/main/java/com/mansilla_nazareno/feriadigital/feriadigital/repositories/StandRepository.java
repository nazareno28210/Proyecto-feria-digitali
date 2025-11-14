package com.mansilla_nazareno.feriadigital.feriadigital.repositories;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Stand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandRepository extends JpaRepository<Stand,Integer> {
    Stand findByFeriante(Feriante feriante);
}
