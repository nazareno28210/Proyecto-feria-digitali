package com.mansilla_nazareno.feriadigital.feriadigital.repositories.auth;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);
}
