package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
