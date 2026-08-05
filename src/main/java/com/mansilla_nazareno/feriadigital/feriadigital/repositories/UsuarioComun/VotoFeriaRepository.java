package com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.VotoFeria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VotoFeriaRepository extends JpaRepository<VotoFeria, Integer> {

    boolean existsByUsuario_IdAndFeria_Id(Integer usuarioId, Integer feriaId);

    Long countByFeria_IdAndEsPositivoTrue(Integer feriaId);

    Long countByFeria_IdAndEsPositivoFalse(Integer feriaId);

    @Query("SELECT COUNT(v) FROM VotoFeria v WHERE v.feria.id = :feriaId AND v.esPositivo = true")
    Long countVotosPositivosFeria(@Param("feriaId") Integer feriaId);

    @Query("SELECT COUNT(v) FROM VotoFeria v WHERE v.feria.id = :feriaId AND v.esPositivo = false")
    Long countVotosNegativosFeria(@Param("feriaId") Integer feriaId);

    @Query("SELECT COUNT(v) FROM VotoFeria v WHERE v.feria.id = :feriaId")
    Long countTotalVotosFeria(@Param("feriaId") Integer feriaId);
}
