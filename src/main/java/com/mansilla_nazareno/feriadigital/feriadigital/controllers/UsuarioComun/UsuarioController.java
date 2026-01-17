package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.RegistroDTO; // 1. IMPORTAR DTO
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun.UsuarioDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ... (deja los métodos getUsuarios, getUsuarioDTO y getCurrentUser igual) ...
    @GetMapping("/usuarios")
    public List<UsuarioDTO> getUsuarios(){
        return usuarioRepository.findAll()
                .stream()
                .map(usuario-> new UsuarioDTO(usuario))
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

    @Autowired
    PasswordEncoder passwordEncoder;

    private boolean esContrasenaSegura(String contrasena) {
        String patron = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?.,;:_-]).{8,}$";
        return contrasena.matches(patron);
    }

    @PostMapping("/usuarios")
    // 2. CAMBIAR PARÁMETRO a @RequestBody RegistroDTO dto
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroDTO dto) {

        // 3. AÑADIR VALIDACIÓN DE COINCIDENCIA
        if (dto.getContrasena() == null || !dto.getContrasena().equals(dto.getConfirmContrasena())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Las contraseñas no coinciden");
        }

        if (usuarioRepository.findByEmail(dto.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El correo ya está registrado");
        }

        if (!esContrasenaSegura(dto.getContrasena())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.");
        }

        // 4. CREAR EL USUARIO real a partir del DTO
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena())); // Codificar

        // Valores por defecto
        usuario.setUserEstate(EstadoUsuario.ACTIVO);
        usuario.setTipoUsuario(TipoUsuario.NORMAL);
        usuario.setFechaRegistro(LocalDate.now());

        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuario registrado correctamente");
    }
}