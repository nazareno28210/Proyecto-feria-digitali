package com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    // ==========================================
    // SECCIÓN PRODUCTOS (Estos funcionan bien)
    // ==========================================
    List<Resena> findByProducto_Id(Integer id);
    boolean existsByUsuario_IdAndProducto_Id(Integer usuarioId, Integer productoId);

    @Query("SELECT AVG(r.puntaje) FROM Resena r WHERE r.producto.id = :productoId")
    Double getPromedioPorProducto(@Param("productoId") Integer productoId);

    @Query("SELECT COUNT(r) FROM Resena r WHERE r.producto.id = :productoId")
    Long getCantidadResenasPorProducto(@Param("productoId") Integer productoId);


    // ==========================================
    // SECCIÓN STANDS (CORREGIDA 🛠️)
    // ==========================================
    // Problema 3 resuelto: Verificamos que sea una reseña de stand PURA (sin producto asociado)
    @Query("SELECT COUNT(r) > 0 FROM Resena r WHERE r.usuario.id = :usuarioId AND r.stand.id = :standId AND r.producto IS NULL")
    boolean existsByUsuario_IdAndStand_IdPuro(@Param("usuarioId") Integer usuarioId, @Param("standId") Integer standId);

    // Aseguramos que el promedio del stand no incluya notas de sus productos
    @Query("SELECT AVG(r.puntaje) FROM Resena r WHERE r.stand.id = :standId AND r.producto IS NULL")
    Double getPromedioPorStand(@Param("standId") Integer standId);

    @Query("SELECT COUNT(r) FROM Resena r WHERE r.stand.id = :standId AND r.producto IS NULL")
    Long getCantidadResenasPorStand(@Param("standId") Integer standId);


    // ==========================================
    // SECCIÓN FERIAS (CORREGIDA 🛠️)
    // ==========================================
    // Problema 1 resuelto: Verificamos que sea un voto de feria PURO (sin stand ni producto)
    @Query("SELECT COUNT(r) > 0 FROM Resena r WHERE r.usuario.id = :usuarioId AND r.feria.id = :feriaId AND r.stand IS NULL AND r.producto IS NULL")
    boolean existsByUsuario_IdAndFeria_IdPuro(@Param("usuarioId") Integer usuarioId, @Param("feriaId") Integer feriaId);

    // Aseguramos que los conteos de feria sean solo votos directos
    @Query("SELECT COUNT(r) FROM Resena r WHERE r.feria.id = :feriaId AND r.puntaje = 5 AND r.stand IS NULL AND r.producto IS NULL")
    Long countVotosPositivosFeria(@Param("feriaId") Integer feriaId);

    @Query("SELECT COUNT(r) FROM Resena r WHERE r.feria.id = :feriaId AND r.stand IS NULL AND r.producto IS NULL")
    Long countTotalVotosFeria(@Param("feriaId") Integer feriaId);
}