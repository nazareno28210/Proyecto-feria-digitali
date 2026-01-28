package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun.CambiarContrasenaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class ContrasenaController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ContrasenaController(UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PutMapping("/change")
    public ResponseEntity<?> cambiarContrasena(
            @RequestBody CambiarContrasenaDTO dto,
            Authentication authentication
    ) {

        if (authentication == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());

        if (usuario == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        // Validar contraseña actual
        if (!passwordEncoder.matches(dto.getContrasenaActual(), usuario.getContrasena())) {
            return ResponseEntity.badRequest().body("Contraseña actual incorrecta");
        }

        // Validar coincidencia
        if (!dto.getNuevacontrasena().equals(dto.getConfirmarNuevacontrasena())) {
            return ResponseEntity.badRequest().body("Las contraseñas no coinciden");
        }

        // Guardar nueva contraseña
        usuario.setContrasena(passwordEncoder.encode(dto.getNuevacontrasena()));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}
