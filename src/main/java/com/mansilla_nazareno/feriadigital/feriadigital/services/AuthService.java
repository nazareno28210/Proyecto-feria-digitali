package com.mansilla_nazareno.feriadigital.feriadigital.services;

import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Rol;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.TokenSeguridad;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.RolRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.TokenSeguridadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TokenSeguridadRepository tokenSeguridadRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==============================
    // REGISTRO
    // ==============================
    @Transactional
    public ResponseEntity<?> registrarUsuario(Usuario usuario) {

        if (usuarioRepository.findByNombreUsuario(usuario.getNombreUsuario()) != null) {
            return ResponseEntity.badRequest().body("El correo ya está registrado");
        }

        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setActivo(false);

        // Buscar o crear rol VISITANTE por defecto
        Rol rolVisitante = rolRepository.findByNombre("VISITANTE")
                .orElseGet(() -> rolRepository.save(new Rol("VISITANTE")));
        usuario.getRoles().add(rolVisitante);

        usuarioRepository.save(usuario);

        // Generar token de verificación
        String token = UUID.randomUUID().toString();
        TokenSeguridad verificationToken = new TokenSeguridad(
                usuario,
                token,
                "VERIFICAR_EMAIL",
                LocalDateTime.now().plusHours(24)
        );

        tokenSeguridadRepository.save(verificationToken);

        emailService.enviarEmail(usuario.getNombreUsuario(), token);

        return ResponseEntity.ok("Usuario registrado. Revisa tu correo.");
    }

    // ==============================
    // VERIFICAR CUENTA
    // ==============================
    @Transactional
    public ResponseEntity<?> verificarCuenta(String token) {

        TokenSeguridad verificationToken = tokenSeguridadRepository
                .findByTokenAndTipoToken(token, "VERIFICAR_EMAIL")
                .orElse(null);

        if (verificationToken == null) {
            return ResponseEntity.badRequest().body("Token inválido");
        }

        if (verificationToken.isExpired()) {
            return ResponseEntity.badRequest().body("Token expirado");
        }

        Usuario usuario = verificationToken.getUsuario();
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        tokenSeguridadRepository.delete(verificationToken);

        return ResponseEntity.ok("Cuenta verificada correctamente");
    }

    // ==============================
    // GENERAR TOKEN RECUPERACIÓN
    // ==============================
    @Transactional
    public void generarTokenRecuperacion(String email) {

        Usuario usuario = usuarioRepository.findByNombreUsuario(email);
        if (usuario == null) return;

        // Eliminar tokens previos de recuperación para este usuario
        tokenSeguridadRepository.deleteByUsuario(usuario);

        String token = UUID.randomUUID().toString();
        TokenSeguridad resetToken = new TokenSeguridad(
                usuario,
                token,
                "RECUPERAR_PASSWORD",
                LocalDateTime.now().plusMinutes(30)
        );

        tokenSeguridadRepository.save(resetToken);

        emailService.enviar(
                usuario.getNombreUsuario(),
                "Recuperación de contraseña - Feria Digital",
                "Click aquí para cambiar tu contraseña:\n" +
                        "http://localhost:8080/web/reset-password.html?token=" + token
        );
    }

    // ==============================
    // RESETEAR PASSWORD
    // ==============================
    @Transactional
    public ResponseEntity<?> resetearPassword(String token, String nuevaPassword) {

        TokenSeguridad resetToken = tokenSeguridadRepository
                .findByTokenAndTipoToken(token, "RECUPERAR_PASSWORD")
                .orElse(null);

        if (resetToken == null) {
            return ResponseEntity.badRequest().body("Token inválido");
        }

        if (resetToken.isExpired()) {
            return ResponseEntity.badRequest().body("Token expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        tokenSeguridadRepository.delete(resetToken);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}
