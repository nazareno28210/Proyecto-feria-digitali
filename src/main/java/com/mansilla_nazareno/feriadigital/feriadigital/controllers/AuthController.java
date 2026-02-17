package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.ResetPasswordDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.PasswordResetToken;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.VerificationToken;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.PasswordResetTokenRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.VerificationTokenRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService,
                          PasswordResetTokenRepository passwordResetTokenRepository,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordResetTokenRepository =passwordResetTokenRepository;
        this.usuarioRepository=usuarioRepository;
        this.passwordEncoder=passwordEncoder;
    }

    @GetMapping("/verificar")
    public ResponseEntity<?> verificarCuenta(@RequestParam String token) {
        return authService.verificarCuenta(token);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        authService.generarTokenRecuperacion(email);
        return ResponseEntity.ok("Si el correo existe, se enviará un link de recuperación.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {

        PasswordResetToken token = passwordResetTokenRepository.findByToken(dto.getToken());

        if (token == null) {
            return ResponseEntity.badRequest().body("Token inválido");
        }

        if (token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expirado");
        }

        Usuario usuario = token.getUsuario();

        usuario.setContrasena(passwordEncoder.encode(dto.getNuevaPassword()));
        usuarioRepository.save(usuario);

        passwordResetTokenRepository.delete(token);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

}