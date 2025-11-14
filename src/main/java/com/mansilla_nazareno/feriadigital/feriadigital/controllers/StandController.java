package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.StandDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.StandUpdateDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StandController {
    @Autowired
    private StandRepository standRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FerianteRepository ferianteRepository;

    @Autowired
    private FeriaRepository feriaRepository;

    public StandController(StandRepository standRepository,
                           UsuarioRepository usuarioRepository,
                           FerianteRepository ferianteRepository,
                           FeriaRepository feriaRepository){
        this.standRepository= standRepository;
        this.usuarioRepository = usuarioRepository;
        this.ferianteRepository = ferianteRepository;
        this.feriaRepository =feriaRepository;
    }
    @GetMapping("/stands")
    public List<StandDTO> getStands(){
        return standRepository.findAll()
                .stream()
                .map(stand-> new StandDTO(stand))
                .toList();
    }
    @GetMapping("/stands/{id}")
    public StandDTO getStandDTO(@PathVariable Integer id){
        return standRepository.findById(id)
                .map(StandDTO::new)
                .orElse(null);

    }
    @PutMapping("/stands/mi-stand")
    public ResponseEntity<?> updateMyStand(Authentication authentication, @RequestBody StandUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);
        Stand stand = standRepository.findByFeriante(feriante);

        if (stand == null) {
            return new ResponseEntity<>("Stand no encontrado", HttpStatus.NOT_FOUND);
        }

        stand.setNombre(dto.getNombre());
        stand.setDescripcion(dto.getDescripcion());
        standRepository.save(stand);

        return new ResponseEntity<>(Map.of("success", "Stand actualizado correctamente"), HttpStatus.OK);
    }


    //-------------------------------------------ENDPOINTS PARA ASIGNAR Y DESASIGNAR STANDS A FERIA -----------------------------------------
    @PatchMapping("/stands/{standId}/asignar-feria/{feriaId}")
    public ResponseEntity<?> asignarStandAFeria(@PathVariable Integer standId, @PathVariable Integer feriaId) {
        Stand stand = standRepository.findById(standId).orElse(null);
        Feria feria = feriaRepository.findById(feriaId).orElse(null);

        if (stand == null) {
            return new ResponseEntity<>("Stand no encontrado", HttpStatus.NOT_FOUND);
        }
        if (feria == null) {
            return new ResponseEntity<>("Feria no encontrada", HttpStatus.NOT_FOUND);
        }

        stand.setFeria(feria);
        standRepository.save(stand);

        return new ResponseEntity<>("Stand asignado a la feria correctamente", HttpStatus.OK);
    }

    // ▼▼▼ NUEVO ENDPOINT PARA DESASIGNAR ▼▼▼
    @PatchMapping("/stands/{standId}/desasignar-feria")
    public ResponseEntity<?> desasignarStandDeFeria(@PathVariable Integer standId) {
        Stand stand = standRepository.findById(standId).orElse(null);

        if (stand == null) {
            return new ResponseEntity<>("Stand no encontrado", HttpStatus.NOT_FOUND);
        }

        // Opcional: Verificar si ya está desasignado
        if (stand.getFeria() == null) {
            return new ResponseEntity<>("El stand no estaba asignado a ninguna feria", HttpStatus.OK);
        }

        stand.setFeria(null); // Setea la feria a null
        standRepository.save(stand);

        return new ResponseEntity<>("Stand desasignado de la feria", HttpStatus.OK);
    }
}
