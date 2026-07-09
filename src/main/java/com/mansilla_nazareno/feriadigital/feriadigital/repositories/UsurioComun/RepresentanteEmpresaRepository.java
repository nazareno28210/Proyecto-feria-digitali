package com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.RepresentanteEmpresa;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Empresa;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepresentanteEmpresaRepository extends JpaRepository<RepresentanteEmpresa, Integer> {
    List<RepresentanteEmpresa> findByEmpresa(Empresa empresa);
    List<RepresentanteEmpresa> findByPersona(Persona persona);
}
