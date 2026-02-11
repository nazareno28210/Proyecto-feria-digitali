package com.mansilla_nazareno.feriadigital.feriadigital.repositories;

import com.mansilla_nazareno.feriadigital.feriadigital.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);
}