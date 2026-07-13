package com.mansilla_nazareno.feriadigital.feriadigital.controllers.auth;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.auth.ResetPasswordDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.TokenSeguridad;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.auth.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.auth.TokenSeguridadRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenSeguridadRepository tokenSeguridadRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService,
                          TokenSeguridadRepository tokenSeguridadRepository,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.tokenSeguridadRepository = tokenSeguridadRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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

        TokenSeguridad token = tokenSeguridadRepository
                .findByTokenAndTipoToken(dto.getToken(), "RECUPERAR_PASSWORD")
                .orElse(null);

        if (token == null) {
            return ResponseEntity.badRequest().body("Token inválido");
        }

        if (token.isExpired()) {
            return ResponseEntity.badRequest().body("Token expirado");
        }

        Usuario usuario = token.getUsuario();

        usuario.setContrasena(passwordEncoder.encode(dto.getNuevaPassword()));
        usuarioRepository.save(usuario);

        tokenSeguridadRepository.delete(token);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}