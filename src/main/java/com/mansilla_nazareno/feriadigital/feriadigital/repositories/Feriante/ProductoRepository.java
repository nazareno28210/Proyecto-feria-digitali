package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto,Integer> {
    List<Producto> findByStand(Stand stand);
}
