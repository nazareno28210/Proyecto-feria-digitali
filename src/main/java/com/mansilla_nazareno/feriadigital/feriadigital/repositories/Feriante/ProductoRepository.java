package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // =============================
    // 👤 USUARIO COMÚN (público)
    // =============================
    // Mostrar solo productos activos
    List<Producto> findByActivoTrue();

    //  Solo activos y NO eliminados
    List<Producto> findByActivoTrueAndEliminadoFalse();

    // 🔍 BUSCADOR DINÁMICO
    @Query("SELECT p FROM Producto p WHERE p.eliminado = false " +
            "AND (:soloActivos = false OR p.activo = true) " +
            "AND (:soloFeriasActivas = false OR p.stand.feria.estado = 'ACTIVA') " +
            "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:catId IS NULL OR p.categoria.id = :catId) " +
            "AND (:feriaId IS NULL OR p.stand.feria.id = :feriaId) " +
            "AND (:minP IS NULL OR p.precio >= :minP) " +
            "AND (:maxP IS NULL OR p.precio <= :maxP)")
    List<Producto> buscarConFiltrosPro(
            String nombre, Integer catId, Integer feriaId,
            Double minP, Double maxP, boolean soloActivos, boolean soloFeriasActivas);

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
