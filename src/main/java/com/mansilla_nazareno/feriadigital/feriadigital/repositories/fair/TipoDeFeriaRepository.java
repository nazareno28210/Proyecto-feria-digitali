package com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair;
import com.mansilla_nazareno.feriadigital.feriadigital.models.feria.TipoDeFeria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoDeFeriaRepository extends JpaRepository<TipoDeFeria, Integer> {
}
