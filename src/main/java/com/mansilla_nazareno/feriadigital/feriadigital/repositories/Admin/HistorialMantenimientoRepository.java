package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.HistorialMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistorialMantenimientoRepository extends JpaRepository<HistorialMantenimiento, Integer> {

    Optional<HistorialMantenimiento> findFirstByEspacioIdAndFechaFinIsNull(Integer espacioId);

}
