package com.mansilla_nazareno.feriadigital.feriadigital.repositories;

import com.mansilla_nazareno.feriadigital.feriadigital.models.SolicitudParaFeriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SolicitudParaFerianteRepository extends JpaRepository<SolicitudParaFeriante, Integer> {
    Optional<SolicitudParaFeriante> findByUsuario(Usuario usuario);
}
