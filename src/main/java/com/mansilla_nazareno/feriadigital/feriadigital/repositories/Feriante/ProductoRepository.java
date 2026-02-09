package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // =============================
    // 👤 USUARIO COMÚN (público)
    // =============================
    // Mostrar solo productos activos
    List<Producto> findByActivoTrue();

    //  Solo activos y NO eliminados
    List<Producto> findByActivoTrueAndEliminadoFalse();

    // =============================
    // 🧑‍🌾 FERIANTE
    // =============================
    // Todos los productos del stand (activos + inactivos)
    List<Producto> findByStand(Stand stand);

    // Solo productos activos del stand
    List<Producto> findByStandAndActivoTrue(Stand stand);

    // Todos sus productos (activos/inactivos) pero NO eliminados
    List<Producto> findByStandAndEliminadoFalse(Stand stand);

    // =============================
    // 🔐 POR AUTENTICACIÓN (rápido)
    // =============================
    // Obtener productos del feriante logueado
    List<Producto> findByStand_Feriante_Usuario_Email(String email);


    List<Producto> findByCategoria_IdAndActivoTrue(int categoriaId);
}
