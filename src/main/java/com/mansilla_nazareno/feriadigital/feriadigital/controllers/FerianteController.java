package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.FerianteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.FerianteUpdateDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class FerianteController {
    @Autowired
    private FerianteRepository ferianteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public FerianteController (FerianteRepository ferianteRepository, UsuarioRepository usuarioRepository){
        this.ferianteRepository=ferianteRepository;
        this.usuarioRepository = usuarioRepository;
    }
    @GetMapping("/feriantes")
    public List<FerianteDTO>getFeriantes(){
        return ferianteRepository.findAll()
                .stream()
                .map(feriante -> new FerianteDTO(feriante))
                .toList();
    }

    @GetMapping("/feriantes/{id}")
    public FerianteDTO getFerianteDTO(@PathVariable Integer id){
        return ferianteRepository.findById(id)
                .map(FerianteDTO::new)
                .orElse(null);
    }
    @GetMapping("/feriantes/current")
    public ResponseEntity<?> getCurrentFeriante(Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>("No estás autenticado", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        if (usuario == null) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        }

        Feriante feriante = ferianteRepository.findByUsuario(usuario);
        if (feriante == null) {
            return new ResponseEntity<>("Este usuario no es un feriante", HttpStatus.FORBIDDEN);
        }

        FerianteDTO ferianteDTO = new FerianteDTO(feriante);
        return new ResponseEntity<>(ferianteDTO, HttpStatus.OK);
    }
    @PutMapping("/feriantes/current")
    public ResponseEntity<?> updateCurrentFeriante(Authentication authentication, @RequestBody FerianteUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);

        if (feriante == null) {
            return new ResponseEntity<>("Feriante no encontrado", HttpStatus.NOT_FOUND);
        }

        // Actualizamos los campos
        feriante.setNombreEmprendimiento(dto.getNombreEmprendimiento());
        feriante.setDescripcion(dto.getDescripcion());
        feriante.setTelefono(dto.getTelefono());
        feriante.setEmailEmprendimiento(dto.getEmailEmprendimiento());

        ferianteRepository.save(feriante);

        return new ResponseEntity<>(Map.of("success", "Perfil de feriante actualizado"), HttpStatus.OK);
    }
}
