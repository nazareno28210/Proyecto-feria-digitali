package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.FeriaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.FerianteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.StandDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.FeriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FeriaController {

    private final FeriaRepository feriaRepository;

    public FeriaController(FeriaRepository feriaRepository) {this.feriaRepository = feriaRepository;}

    @GetMapping("/ferias")
    public List<FeriaDTO> getFerias() {
        return feriaRepository.findAll()
                .stream()
                .map(feria -> new FeriaDTO(feria))
                .collect(Collectors.toList());
    }

    @GetMapping("/ferias/{id}")
    public FeriaDTO getFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id)
                .map(FeriaDTO::new)
                .orElse(null);
    }

    //obtener ferias activas
    @GetMapping("/ferias/activas")
    public List<FeriaDTO> getFeriasActivas() {
        return feriaRepository.findByEstado("Activa")
                .stream()
                .map(FeriaDTO::new)
                .collect(Collectors.toList());
    }

    //crear feria
    @PostMapping("/ferias")
    public ResponseEntity<?> crearFeria(@RequestBody Feria nuevaFeria) {
        nuevaFeria.setEstado("Activa");
        feriaRepository.save(nuevaFeria);
        return ResponseEntity.ok("Feria creada correctamente");
    }

    //actualizar feria
    @PutMapping("/ferias/{id}")
    public ResponseEntity<?> actualizarFeria(@PathVariable Integer id, @RequestBody Feria feriaActualizada) {
        return feriaRepository.findById(id).map(feria -> {
            feria.setNombre(feriaActualizada.getNombre());
            feria.setLugar(feriaActualizada.getLugar());
            feria.setDescripcion(feriaActualizada.getDescripcion());
            feria.setFechaInicio(feriaActualizada.getFechaInicio());
            feria.setFechaFinal(feriaActualizada.getFechaFinal());
            feria.setImagenUrl(feriaActualizada.getImagenUrl());
            feriaRepository.save(feria);
            return ResponseEntity.ok("Feria actualizada correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }
    //dar de baja feria
    @PatchMapping("/ferias/{id}/baja")
    public ResponseEntity<?> darDeBaja(@PathVariable Integer id) {
        return feriaRepository.findById(id).map(feria -> {
            feria.setEstado("Inactiva");
            feriaRepository.save(feria);
            return ResponseEntity.ok("Feria dada de baja");
        }).orElse(ResponseEntity.notFound().build());
    }

    //eliminar feria
    @DeleteMapping("/ferias/{id}")
    public ResponseEntity<?> eliminarFeria(@PathVariable Integer id) {
        if (!feriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        feriaRepository.deleteById(id);
        return ResponseEntity.ok("Feria eliminada correctamente");
    }
    //activar feria
    @PatchMapping("/ferias/{id}/activar")
    public ResponseEntity<?> activarFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id).map(feria -> {
            feria.setEstado("Activa");
            feriaRepository.save(feria);
            return ResponseEntity.ok("Feria activada correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }

}
