package com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.ResenaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaProductoRepository extends JpaRepository<ResenaProducto, Integer> {

    List<ResenaProducto> findByProducto_Id(Integer productoId);

    boolean existsByUsuario_IdAndProducto_Id(Integer usuarioId, Integer productoId);

    @Query("SELECT AVG(r.puntaje) FROM ResenaProducto r WHERE r.producto.id = :productoId")
    Double getPromedioPorProducto(@Param("productoId") Integer productoId);

    @Query("SELECT COUNT(r) FROM ResenaProducto r WHERE r.producto.id = :productoId")
    Long getCantidadResenasPorProducto(@Param("productoId") Integer productoId);
}
