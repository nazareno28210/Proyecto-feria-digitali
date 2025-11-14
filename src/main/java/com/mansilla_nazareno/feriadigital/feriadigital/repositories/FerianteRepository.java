package com.mansilla_nazareno.feriadigital.feriadigital.repositories;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FerianteRepository extends JpaRepository <Feriante, Integer>{
    Feriante findByUsuario(Usuario usuario);
}
