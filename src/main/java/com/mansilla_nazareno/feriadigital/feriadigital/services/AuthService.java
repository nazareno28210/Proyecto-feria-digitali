package com.mansilla_nazareno.feriadigital.feriadigital.services;

import com.mansilla_nazareno.feriadigital.feriadigital.models.PasswordResetToken;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.VerificationToken;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.PasswordResetTokenRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    // ==============================
    // REGISTRO
    // ==============================
    public ResponseEntity<?> registrarUsuario(Usuario usuario) {

        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.badRequest().body("El correo ya está registrado");
        }

        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setEnabled(false);

        usuarioRepository.save(usuario);

        // Generar token
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUsuario(usuario);
        verificationToken.setFechaExpiracion(LocalDateTime.now().plusHours(24));

        verificationTokenRepository.save(verificationToken);

        emailService.enviarEmail(usuario.getEmail(), token);

        return ResponseEntity.ok("Usuario registrado. Revisa tu correo.");
    }

    // ==============================
    // VERIFICAR CUENTA
    // ==============================
    public ResponseEntity<?> verificarCuenta(String token) {

        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            return ResponseEntity.badRequest().body("Token inválido");
        }

        if (verificationToken.getFechaExpiracion()
                .isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expirado");
        }

        Usuario usuario = verificationToken.getUsuario();
        usuario.setEnabled(true);
        usuarioRepository.save(usuario);

        verificationTokenRepository.delete(verificationToken);

        return ResponseEntity.ok("Cuenta verificada correctamente");
    }


    public void generarTokenRecuperacion(String email) {

        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) return;

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));

        passwordResetTokenRepository.save(resetToken);

        emailService.enviar(
                usuario.getEmail(),
                "Recuperación de contraseña - Feria Digital",
                "Click aquí para cambiar tu contraseña:\n" +
                        "http://localhost:8080/auth/reset-password?token=" + token );
    }
    public ResponseEntity<?> resetearPassword(String token, String nuevaPassword) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByToken(token);

        if (resetToken == null) {
            return ResponseEntity.badRequest().body("Token inválido");
        }

        if (resetToken.getFechaExpiracion()
                .isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expirado");
        }

        Usuario usuario = resetToken.getUsuario();

        usuario.setContrasena(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        passwordResetTokenRepository.delete(resetToken);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }


}

