package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Espacio;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoEspacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EspacioRepository extends JpaRepository<Espacio, Integer> {

    List<Espacio> findByEdicionId(Integer edicionId);

    boolean existsByEdicionIdAndNombre(Integer edicionId, String nombre);

    boolean existsByEdicionIdAndNombreAndEstadoNot(Integer edicionId, String nombre, EstadoEspacio estado);
}
