package com.mansilla_nazareno.feriadigital.feriadigital.repositories.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    Optional<Empresa> findByCuit(String cuit);
}
