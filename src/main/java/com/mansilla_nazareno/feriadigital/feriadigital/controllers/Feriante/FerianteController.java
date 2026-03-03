package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.FerianteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.FerianteUpdateDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FerianteController {
    @Autowired
    private FerianteRepository ferianteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

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
        System.out.println("Recibiendo actualización para: " + dto.getNombreEmprendimiento()); // 🟢 Debug

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);

        if (feriante == null) {
            return new ResponseEntity<>("Feriante no encontrado", HttpStatus.NOT_FOUND);
        }

        // Actualizamos solo si el dato no es nulo
        if(dto.getNombreEmprendimiento() != null) feriante.setNombreEmprendimiento(dto.getNombreEmprendimiento());
        if(dto.getDescripcion() != null) feriante.setDescripcion(dto.getDescripcion());
        if(dto.getTelefono() != null) feriante.setTelefono(dto.getTelefono());
        if(dto.getEmailEmprendimiento() != null) feriante.setEmailEmprendimiento(dto.getEmailEmprendimiento());

        ferianteRepository.save(feriante);
        return new ResponseEntity<>(Map.of("message", "Perfil de feriante actualizado"), HttpStatus.OK);
    }

    @PatchMapping("/feriantes/current/imagen")
    public ResponseEntity<?> updateFerianteImage(Authentication authentication, @RequestParam("imagen") MultipartFile imagen) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());

        if (usuario == null) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        }

        try {
            // Usamos tu método del servicio.
            // Si ya tenía una foto, podrías usar reemplazarImagen, pero para simplificar:
            Map<String, String> result = cloudinaryService.subirImagen(imagen);
            String imageUrl = result.get("url");

            // Guardamos la URL en el objeto Usuario (que es el que tiene la imagen de perfil)
            usuario.setImagenUrl(imageUrl);
            usuarioRepository.save(usuario);

            return new ResponseEntity<>(imageUrl, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al procesar la imagen: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
