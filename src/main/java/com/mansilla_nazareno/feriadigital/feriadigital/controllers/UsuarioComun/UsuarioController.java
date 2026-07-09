package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.RegistroDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun.UsuarioDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Persona;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.AuthService;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             AuthService authService,
                             CloudinaryService cloudinaryService) {
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioDTO> getUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioDTO::new)
                .toList();
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioDTO getUsuarioDTO(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(UsuarioDTO::new)
                .orElse(null);
    }

    @GetMapping("/usuarios/current")
    public UsuarioDTO getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Usuario usuario = usuarioRepository.findByNombreUsuario(authentication.getName());
        return new UsuarioDTO(usuario);
    }

    @PostMapping("/usuarios/current")
    public ResponseEntity<?> updateCurrentUser(Authentication authentication, @RequestBody RegistroDTO dto) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No hay una sesión activa");
        }

        Usuario usuario = usuarioRepository.findByNombreUsuario(authentication.getName());
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Persona persona = usuario.getPersona();
        if (persona == null) {
            persona = new Persona();
            usuario.setPersona(persona);
        }

        // 1. Actualizar datos básicos
        persona.setNombre(dto.getNombre());
        persona.setApellido(dto.getApellido());

        // Validar si el email cambió y si ya existe
        if (!usuario.getNombreUsuario().equals(dto.getEmail())) {
            if (usuarioRepository.findByNombreUsuario(dto.getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El nuevo correo ya está registrado");
            }
            usuario.setNombreUsuario(dto.getEmail());
        }

        // 2. Lógica para cambiar contraseña (solo si se envió una nueva)
        if (dto.getContrasena() != null && !dto.getContrasena().isBlank()) {
            if (!dto.getContrasena().equals(dto.getConfirmContrasena())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Las contraseñas no coinciden");
            }

            if (!esContrasenaSegura(dto.getContrasena())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La nueva contraseña no cumple con los requisitos de seguridad.");
            }
            usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Perfil actualizado correctamente");
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroDTO dto) {

        if (dto.getContrasena() == null || !dto.getContrasena().equals(dto.getConfirmContrasena())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Las contraseñas no coinciden");
        }

        if (usuarioRepository.findByNombreUsuario(dto.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado");
        }

        if (!esContrasenaSegura(dto.getContrasena())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.");
        }

        Persona persona = new Persona(dto.getNombre(), dto.getApellido(), null, null, null);
        Usuario usuario = new Usuario(persona, dto.getEmail(), dto.getContrasena());

        return authService.registrarUsuario(usuario);
    }

    @PostMapping("/password/cambiar")
    public ResponseEntity<?> cambiarPassword(Authentication authentication, @RequestBody Map<String, String> body) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesión no válida");
        }

        String passwordActual = body.get("passwordActual");
        String passwordNueva = body.get("passwordNueva");

        Usuario usuario = usuarioRepository.findByNombreUsuario(authentication.getName());

        // 1. Verificar que la contraseña actual coincida con la de la base de datos
        if (!passwordEncoder.matches(passwordActual, usuario.getContrasena())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La contraseña actual es incorrecta");
        }

        // 2. Validar seguridad de la nueva contraseña
        if (!esContrasenaSegura(passwordNueva)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La nueva contraseña no cumple con los requisitos de seguridad.");
        }

        // 3. Encriptar y guardar
        usuario.setContrasena(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    private boolean esContrasenaSegura(String contrasena) {
        String patron = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?.,;:_-]).{8,}$";
        return contrasena.matches(patron);
    }

    @PatchMapping(value = "/usuarios/current/imagen", consumes = {"multipart/form-data"})
    public ResponseEntity<?> subirFotoPerfil(@RequestParam("imagen") MultipartFile imagen,
                                             Authentication authentication) {
        if (imagen == null || imagen.isEmpty()) {
            return ResponseEntity.badRequest().body("No se envió ninguna imagen");
        }

        Usuario usuario = usuarioRepository.findByNombreUsuario(authentication.getName());
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Persona persona = usuario.getPersona();
        if (persona == null) {
            persona = new Persona();
            usuario.setPersona(persona);
        }

        Map<String, String> result;
        if (persona.getImagenPublicId() != null) {
            result = cloudinaryService.reemplazarImagen(imagen, persona.getImagenPublicId());
        } else {
            result = cloudinaryService.subirImagen(imagen);
        }

        persona.setImagenUrl(result.get("url"));
        persona.setImagenPublicId(result.get("public_id"));

        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Foto de perfil actualizada");
    }
}