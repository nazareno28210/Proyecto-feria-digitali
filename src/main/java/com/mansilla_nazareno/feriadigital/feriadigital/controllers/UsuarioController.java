package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioRepository;
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
    // 🔹 NUEVO ENDPOINT PARA LOGIN
    @GetMapping("/usuarios/current")
    public UsuarioDTO getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null; // No hay usuario logueado
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
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {

        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El correo ya está registrado");
        }

        // Codificar contraseña
        if (!esContrasenaSegura(usuario.getContrasena())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.");
        }
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));

        // Valores por defecto
        if (usuario.getEstadoUsuario() == null) usuario.setUserEstate(EstadoUsuario.ACTIVO);
        if (usuario.getTipoUsuario() == null) usuario.setTipoUsuario(TipoUsuario.NORMAL);
        if (usuario.getFechaRegistro() == null) usuario.setFechaRegistro(LocalDate.now());

        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuario registrado correctamente");
    }



}
