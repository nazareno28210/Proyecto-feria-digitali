package com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.RecordatorioEdicion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordatorioEdicionRepository extends JpaRepository<RecordatorioEdicion, Long> {

    Optional<RecordatorioEdicion> findByUsuarioAndEdicion(Usuario usuario, EdicionFeria edicion);

    boolean existsByUsuarioAndEdicionAndActivoTrue(Usuario usuario, EdicionFeria edicion);

    List<RecordatorioEdicion> findByEdicionAndActivoTrueAndNotificadoFalse(EdicionFeria edicion);

    List<RecordatorioEdicion> findByNotificadoFalse();
}

