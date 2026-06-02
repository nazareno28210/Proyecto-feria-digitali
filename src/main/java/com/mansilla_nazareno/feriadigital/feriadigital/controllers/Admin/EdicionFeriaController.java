package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.EdicionFeriaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ediciones")
public class EdicionFeriaController {

    @Autowired
    private EdicionFeriaRepository edicionFeriaRepository;

    @Autowired
    private FeriaRepository feriaRepository;

    // Obtener todas las ediciones (Historial general)
    @GetMapping
    public ResponseEntity<List<EdicionFeriaDTO>> listarTodas() {
        List<EdicionFeriaDTO> lista = edicionFeriaRepository.findAll().stream()
                .map(EdicionFeriaDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // Obtener solo las ediciones abiertas (Para inscripción de feriantes)
    @GetMapping("/activas")
    public ResponseEntity<List<EdicionFeriaDTO>> listarActivas() {
        List<EdicionFeriaDTO> activas = edicionFeriaRepository.findByEstado("ACTIVA").stream()
                .map(EdicionFeriaDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(activas);
    }

    // Crear una nueva edición vinculada a una feria base
    @PostMapping
    public ResponseEntity<?> crearEdicion(@RequestBody EdicionFeriaDTO dto) {
        Feria feria = feriaRepository.findById(dto.getFeriaId()).orElse(null);

        if (feria == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "La feria base seleccionada no existe"));
        }

        EdicionFeria nuevaEdicion = new EdicionFeria();
        nuevaEdicion.setFeria(feria);
        nuevaEdicion.setNombreEdicion(dto.getNombreEdicion());
        nuevaEdicion.setFechaInicio(dto.getFechaInicio());
        nuevaEdicion.setFechaFinal(dto.getFechaFinal());

        // 🟢 NUEVO: Atrapamos los horarios que vienen del JSON del Frontend
        nuevaEdicion.setHoraInicio(dto.getHoraInicio());
        nuevaEdicion.setHoraFin(dto.getHoraFin());

        nuevaEdicion.setEstado("ACTIVA"); // Arranca activa por defecto para recibir postulaciones

        EdicionFeria guardada = edicionFeriaRepository.save(nuevaEdicion);
        return ResponseEntity.ok(new EdicionFeriaDTO(guardada));
    }

    // Cambiar estado (Para finalizar o cancelar un evento)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam String nuevoEstado) {
        EdicionFeria edicion = edicionFeriaRepository.findById(id).orElse(null);
        if (edicion == null) return ResponseEntity.notFound().build();

        edicion.setEstado(nuevoEstado.toUpperCase());
        edicionFeriaRepository.save(edicion);
        return ResponseEntity.ok(Map.of("mensaje", "Estado de la edición actualizado con éxito"));
    }

    // 🟢 NUEVO: Obtener una edición específica por su ID
    @GetMapping("/{id}")
    public ResponseEntity<EdicionFeriaDTO> obtenerPorId(@PathVariable Integer id) {
        EdicionFeria edicion = edicionFeriaRepository.findById(id).orElse(null);
        if (edicion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new EdicionFeriaDTO(edicion));
    }
}