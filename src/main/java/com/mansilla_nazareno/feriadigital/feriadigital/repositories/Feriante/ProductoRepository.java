package com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // =============================
    // 👤 USUARIO COMÚN (público)
    // =============================
    List<Producto> findByActivoTrue();

    List<Producto> findByActivoTrueAndEliminadoFalse();

    // 🟢 🔍 BUSCADOR DINÁMICO REFACTOREADO
    @Query("SELECT DISTINCT p FROM Producto p " +
            "LEFT JOIN p.stand.participaciones part " +
            "WHERE p.eliminado = false " +
            "AND (:soloActivos = false OR p.activo = true) " +
            "AND (:soloFeriasActivas = false OR (part.edicion.estado = 'ACTIVA' AND p.stand.activo = true)) " + // 🟢 CORREGIDO: part.edicion.estado
            "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:catId IS NULL OR p.categoria.id = :catId) " +
            "AND (:feriaId IS NULL OR part.edicion.feria.id = :feriaId) " + // 🟢 CORREGIDO: part.edicion.feria.id
            "AND (:minP IS NULL OR p.precio >= :minP) " +
            "AND (:maxP IS NULL OR p.precio <= :maxP)")
    List<Producto> buscarConFiltrosPro(
            @Param("nombre") String nombre,
            @Param("catId") Integer catId,
            @Param("feriaId") Integer feriaId,
            @Param("minP") Double minP,
            @Param("maxP") Double maxP,
            @Param("soloActivos") boolean soloActivos,
            @Param("soloFeriasActivas") boolean soloFeriasActivas);

    // =============================
    // 🧑‍🌾 FERIANTE
    // =============================
    List<Producto> findByStand(Stand stand);

    List<Producto> findByStandAndActivoTrue(Stand stand);

    List<Producto> findByStandAndEliminadoFalse(Stand stand);

    // =============================
    // 🔐 POR AUTENTICACIÓN (rápido)
    // =============================
    List<Producto> findByStand_Feriante_Usuario_Email(String email);

    List<Producto> findByCategoria_IdAndActivoTrue(int categoriaId);
}