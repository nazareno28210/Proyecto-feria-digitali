package com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.SolicitudParaFeriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SolicitudParaFerianteRepository extends JpaRepository<SolicitudParaFeriante, Integer> {
    Optional<SolicitudParaFeriante> findByUsuario(Usuario usuario);
}
