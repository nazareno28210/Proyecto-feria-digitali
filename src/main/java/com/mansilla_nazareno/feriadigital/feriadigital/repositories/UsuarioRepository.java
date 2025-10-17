package com.mansilla_nazareno.feriadigital.feriadigital.repositories;

import com.mansilla_nazareno.feriadigital.feriadigital.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository< User, Integer> {
    List<User> findByNombre(String nombre);
    User findByemail(String email);
}
