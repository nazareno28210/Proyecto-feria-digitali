package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.RegistroDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun.UsuarioDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.VerificationToken;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.VerificationTokenRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import com.mansilla_nazareno.feriadigital.feriadigital.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository
            ,CloudinaryService cloudinaryService
            ,VerificationTokenRepository verificationTokenRepository
            ,EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.cloudinaryService = cloudinaryService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService=emailService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioDTO> getUsuarios(){
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioDTO::new)
                .toList();
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioDTO getUsuarioDTO(@PathVariable Integer id){
        return usuarioRepository.findById(id)
                .map(UsuarioDTO::new)
                .orElse(null);
    }

    @GetMapping("/usuarios/current")
    public UsuarioDTO getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        return new UsuarioDTO(usuario);
    }

    // MÉTODO: Actualizar perfil y contraseña 🟢
    @PostMapping("/usuarios/current")
    public ResponseEntity<?> updateCurrentUser(Authentication authentication, @RequestBody RegistroDTO dto) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No hay una sesión activa");
        }

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        // 1. Actualizar datos básicos
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());

        //  Validar si el email cambió y si ya existe
        if (!usuario.getEmail().equals(dto.getEmail())) {
            if (usuarioRepository.findByEmail(dto.getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El nuevo correo ya está registrado");
            }
            usuario.setEmail(dto.getEmail());
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

        if (usuarioRepository.findByEmail(dto.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado");
        }

        if (!esContrasenaSegura(dto.getContrasena())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setUserEstate(EstadoUsuario.ACTIVO);
        usuario.setTipoUsuario(TipoUsuario.NORMAL);
        usuario.setFechaRegistro(LocalDate.now());

        // 🔐 IMPORTANTE
        usuario.setEnabled(false);

        usuarioRepository.save(usuario);

        // 🔑 Generar token
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUsuario(usuario);
        verificationToken.setFechaExpiracion(LocalDateTime.now().plusHours(24));

        verificationTokenRepository.save(verificationToken);

        // 📧 Enviar email
        emailService.enviarEmail(usuario.getEmail(), token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuario registrado. Revisa tu correo para verificar tu cuenta.");
    }



    @PostMapping("/password/cambiar")
    public ResponseEntity<?> cambiarPassword(Authentication authentication, @RequestBody java.util.Map<String, String> body) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesión no válida");
        }

        String passwordActual = body.get("passwordActual");
        String passwordNueva = body.get("passwordNueva");

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());

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
            Authentication authentication
    ) {
        if (imagen == null || imagen.isEmpty()) {return ResponseEntity.badRequest().body("No se envió ninguna imagen");}

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Map<String, String> result;
        if (usuario.getImagenPublicId() != null) {
            result = cloudinaryService.reemplazarImagen(
                    imagen,
                    usuario.getImagenPublicId()
            );
        } else {result = cloudinaryService.subirImagen(imagen);}

        usuario.setImagenUrl(result.get("url"));
        usuario.setImagenPublicId(result.get("public_id"));

        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Foto de perfil actualizada");
    }


}