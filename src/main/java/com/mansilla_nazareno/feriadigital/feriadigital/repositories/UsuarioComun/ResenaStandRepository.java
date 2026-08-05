package com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.ResenaStand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResenaStandRepository extends JpaRepository<ResenaStand, Integer> {

    boolean existsByUsuario_IdAndStand_Id(Integer usuarioId, Integer standId);

    @Query("SELECT AVG(r.puntaje) FROM ResenaStand r WHERE r.stand.id = :standId")
    Double getPromedioPorStand(@Param("standId") Integer standId);

    @Query("SELECT COUNT(r) FROM ResenaStand r WHERE r.stand.id = :standId")
    Long getCantidadResenasPorStand(@Param("standId") Integer standId);

    Long countByStand_Id(Integer standId);
}
