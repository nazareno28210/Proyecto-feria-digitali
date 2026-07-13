package com.mansilla_nazareno.feriadigital.feriadigital.repositories.auth;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
}
