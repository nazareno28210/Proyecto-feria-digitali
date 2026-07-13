package com.mansilla_nazareno.feriadigital.feriadigital.repositories.participant;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.RepresentanteEmpresa;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Empresa;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepresentanteEmpresaRepository extends JpaRepository<RepresentanteEmpresa, Integer> {
    List<RepresentanteEmpresa> findByEmpresa(Empresa empresa);
    List<RepresentanteEmpresa> findByPersona(Persona persona);
}
