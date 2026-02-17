package com.mansilla_nazareno.feriadigital.feriadigital.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviar(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }


    public void enviarEmail(String to, String token) {
        String link = "http://localhost:8080/web/reset-password.html?token=" + token;



        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(to);
        mensaje.setSubject("Verifica tu cuenta - Feria Digital");
        mensaje.setText("Haz clic en el siguiente enlace para activar tu cuenta:\n" + link);

        mailSender.send(mensaje);
    }
}
