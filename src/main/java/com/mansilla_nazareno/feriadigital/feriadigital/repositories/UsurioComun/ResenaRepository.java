package com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    // Buscar reseñas específicas para un producto
    List<Resena> findByProducto_Id(Integer productoId);

    // Buscar reseñas para un stand
    List<Resena> findByStand_Id(Integer standId);

    // Buscar reseñas para una feria
    List<Resena> findByFeria_Id(Integer feriaId);

    boolean existsByUsuario_IdAndProducto_Id(Integer usuarioId, Integer productoId);
}