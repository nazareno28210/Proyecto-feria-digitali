package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FerianteRepository extends JpaRepository <Feriante, Integer>{
    Feriante findByUsuario(Usuario usuario);
}
