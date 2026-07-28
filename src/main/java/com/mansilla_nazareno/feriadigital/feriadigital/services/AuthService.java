package com.mansilla_nazareno.feriadigital.feriadigital.services;

import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoToken;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioToken;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioTokenService usuarioTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       UsuarioTokenService usuarioTokenService,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioTokenService = usuarioTokenService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ResponseEntity<?> registrarUsuario(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado");
        }

        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setEnabled(false);
        usuario.setUserEstate(EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);

        UsuarioToken token = usuarioTokenService.generarToken(usuario, TipoToken.CONFIRMACION_CUENTA);

        emailService.enviarEmail(usuario.getEmail(), token.getToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuario registrado. Revisa tu correo para verificar tu cuenta.");
    }

    @Transactional
    public boolean verificarCuenta(String tokenStr) {
        Optional<UsuarioToken> tokenOpt = usuarioTokenService.validarToken(tokenStr, TipoToken.CONFIRMACION_CUENTA);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        UsuarioToken usuarioToken = tokenOpt.get();
        Usuario usuario = usuarioToken.getUsuario();
        usuario.setEnabled(true);
        usuario.setUserEstate(EstadoUsuario.ACTIVO);
        usuarioRepository.save(usuario);

        usuarioTokenService.marcarComoUsado(usuarioToken);

        return true;
    }

    @Transactional
    public void generarTokenRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) return;

        UsuarioToken token = usuarioTokenService.generarToken(usuario, TipoToken.RECUPERACION_CONTRASENA);

        emailService.enviar(
                usuario.getEmail(),
                "Recuperación de contraseña - Feria Digital",
                "Haz clic en el siguiente enlace para cambiar tu contraseña (válido por 15 minutos):\n" +
                        "http://localhost:8080/web/reset-password.html?token=" + token.getToken()
        );
    }

    @Transactional
    public ResponseEntity<?> resetearPassword(String tokenStr, String nuevaPassword) {
        Optional<UsuarioToken> tokenOpt = usuarioTokenService.validarToken(tokenStr, TipoToken.RECUPERACION_CONTRASENA);

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Token inválido o expirado");
        }

        if (!esContrasenaSegura(nuevaPassword)) {
            return ResponseEntity.badRequest()
                    .body("La nueva contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.");
        }

        UsuarioToken usuarioToken = tokenOpt.get();
        Usuario usuario = usuarioToken.getUsuario();

        usuario.setContrasena(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        usuarioTokenService.marcarComoUsado(usuarioToken);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    private boolean esContrasenaSegura(String contrasena) {
        if (contrasena == null) return false;
        String patron = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?.,;:_-]).{8,}$";
        return contrasena.matches(patron);
    }
}
