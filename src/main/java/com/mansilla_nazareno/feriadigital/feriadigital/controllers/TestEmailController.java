package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestEmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/test-mail")
    public String testMail() {
        emailService.enviar(
                "giandenismansilla@gmail.com",
                "Feria Digital – Test",
                "Si llegó este mail, Gmail SMTP funciona correctamente 🚀"
        );
        return "Mail enviado correctamente";
    }
}