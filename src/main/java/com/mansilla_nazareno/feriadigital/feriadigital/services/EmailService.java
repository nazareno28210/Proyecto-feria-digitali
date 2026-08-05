package com.mansilla_nazareno.feriadigital.feriadigital.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void enviar(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }


    public void enviarEmail(String to, String token) {
        String link = baseUrl + "/auth/verificar?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(to);
        mensaje.setSubject("Verifica tu cuenta - Feria Digital");
        mensaje.setText("Haz clic en el siguiente enlace para activar tu cuenta:\n" + link);

        mailSender.send(mensaje);
    }

    // 📩 Enviar email cuando una solicitud a Feriante es APROBADA
    public void enviarEmailAprobacionSolicitud(String destinatario, String nombreUsuario, String nombreEmprendimiento) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("¡Felicidades! Tu solicitud para ser Feriante ha sido aprobada — Feria Digital");

            String texto = String.format(
                "Hola %s,\n\n" +
                "¡Nos alegra informarte que tu solicitud para formar parte de Feria Digital con el emprendimiento '%s' ha sido APROBADA!\n\n" +
                "Tu cuenta ha sido actualizada exitosamente al rol de FERIANTE. Ahora puedes ingresar a la plataforma para administrar tu perfil comercial y solicitar la asignación de puestos en las ferias disponibles.\n\n" +
                "Además, hemos creado tu puesto (Stand) por defecto con la información de tu emprendimiento para que comiences de inmediato.\n\n" +
                "¡Muchos éxitos!\n" +
                "El equipo de Feria Digital",
                nombreUsuario != null ? nombreUsuario : "Feriante",
                nombreEmprendimiento != null ? nombreEmprendimiento : "tu emprendimiento"
            );

            mensaje.setText(texto);
            mailSender.send(mensaje);
            System.out.println("✅ Email de aprobación enviado a: " + destinatario);
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo enviar el correo de aprobación a " + destinatario + ": " + e.getMessage());
        }
    }

    // 📩 Enviar email cuando una solicitud a Feriante es RECHAZADA (Con motivo)
    public void enviarEmailRechazoSolicitud(String destinatario, String nombreUsuario, String nombreEmprendimiento, String motivoRechazo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Actualización sobre tu solicitud para ser Feriante — Feria Digital");

            String texto = String.format(
                "Hola %s,\n\n" +
                "Te escribimos para informarte sobre el estado de tu postulación para ser Feriante con el emprendimiento '%s'.\n\n" +
                "Lamentamos informarte que tu solicitud ha sido desestimada en esta ocasión.\n\n" +
                "📌 Motivo indicado por la administración:\n\"%s\"\n\n" +
                "Puedes revisar las observaciones realizadas y volver a enviar una postulación corregida desde la plataforma cuando lo desees.\n\n" +
                "Atentamente,\n" +
                "El equipo de Feria Digital",
                nombreUsuario != null ? nombreUsuario : "Usuario",
                nombreEmprendimiento != null ? nombreEmprendimiento : "tu emprendimiento",
                motivoRechazo != null ? motivoRechazo : "No se especificó un motivo particular."
            );

            mensaje.setText(texto);
            mailSender.send(mensaje);
            System.out.println("✅ Email de rechazo enviado a: " + destinatario);
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo enviar el correo de rechazo a " + destinatario + ": " + e.getMessage());
        }
    }
}

