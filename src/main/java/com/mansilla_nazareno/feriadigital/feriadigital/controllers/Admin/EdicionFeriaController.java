package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.EdicionFeriaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
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

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ParticipacionRepository participacionRepository;

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
                .map(edicion -> {
                    EdicionFeriaDTO dto = new EdicionFeriaDTO(edicion);
                    // Calculamos cupos ocupados para que el frontend muestre la advertencia
                    long ocupados = participacionRepository.findByEdicionId(edicion.getId()).stream()
                            .filter(p -> p.getEstado().name().equals("CONFIRMADO") || p.getEstado().name().equals("PENDIENTE"))
                            .count();
                    dto.setCuposOcupados((int) ocupados);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(activas);
    }

    // Crear una nueva edición vinculada a una feria base
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> crearEdicion(
            @RequestParam Integer feriaId,
            @RequestParam String nombreEdicion,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFinal,
            @RequestParam String horaInicio,
            @RequestParam String horaFin,
            @RequestParam(value = "capacidad", required = false) Integer capacidad,
            @RequestParam(value = "mapa", required = false) MultipartFile mapa) {

        Feria feria = feriaRepository.findById(feriaId).orElse(null);
        if (feria == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "La feria base seleccionada no existe"));
        }

        if (capacidad != null && capacidad <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "La capacidad de stands debe ser mayor a 0"));
        }

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = (fechaFinal != null && !fechaFinal.isEmpty()) ? LocalDate.parse(fechaFinal) : null;

        // 🛡️ VALIDACIÓN: fechaFin no puede ser anterior a fechaInicio
        if (fin != null && fin.isBefore(inicio)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La fecha de fin no puede ser anterior a la fecha de inicio."));
        }

        if (fin != null && edicionFeriaRepository.existeSolapamiento(feria.getId(), inicio, fin, null)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las fechas se solapan con otra edición activa de esta feria"));
        }

        EdicionFeria nuevaEdicion = new EdicionFeria();
        nuevaEdicion.setFeria(feria);
        nuevaEdicion.setNombreEdicion(nombreEdicion);
        nuevaEdicion.setFechaInicio(inicio);
        nuevaEdicion.setFechaFinal(fin);
        nuevaEdicion.setHoraInicio(LocalTime.parse(horaInicio));
        nuevaEdicion.setHoraFin(LocalTime.parse(horaFin));
        nuevaEdicion.setEstado("ACTIVA");
        nuevaEdicion.setCapacidad(capacidad);

        if (mapa != null && !mapa.isEmpty()) {
            Map<String, String> resultado = cloudinaryService.subirImagen(mapa);
            nuevaEdicion.setMapaUrl(resultado.get("url"));
            nuevaEdicion.setMapaPublicId(resultado.get("public_id"));
        }

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

    // Obtener una edición específica por su ID
    @GetMapping("/{id}")
    public ResponseEntity<EdicionFeriaDTO> obtenerPorId(@PathVariable Integer id) {
        EdicionFeria edicion = edicionFeriaRepository.findById(id).orElse(null);
        if (edicion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new EdicionFeriaDTO(edicion));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> actualizarEdicion(
            @PathVariable Integer id,
            @RequestParam Integer feriaId,
            @RequestParam String nombreEdicion,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFinal,
            @RequestParam String horaInicio,
            @RequestParam String horaFin,
            @RequestParam(value = "capacidad", required = false) Integer capacidad,
            @RequestParam(value = "mapa", required = false) MultipartFile mapa) {

        EdicionFeria edicion = edicionFeriaRepository.findById(id).orElse(null);
        if (edicion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "La edición no existe"));
        }

        if (capacidad != null && capacidad <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "La capacidad de stands debe ser mayor a 0"));
        }

        if (capacidad != null && edicion.getCapacidad() != null && capacidad < edicion.getCapacidad()) {
            long ocupantes = participacionRepository.findByEdicionId(id).stream()
                    .filter(p -> p.getEstado().name().equals("CONFIRMADO") || p.getEstado().name().equals("PENDIENTE"))
                    .count();
            if (capacidad < ocupantes) {
                return ResponseEntity.badRequest().body(Map.of("error", "No puedes reducir la capacidad. La edición ya tiene más feriantes activos (" + ocupantes + ") que el nuevo límite."));
            }
        }

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = (fechaFinal != null && !fechaFinal.isEmpty()) ? LocalDate.parse(fechaFinal) : null;

        // 🛡️ VALIDACIÓN: fechaFin no puede ser anterior a fechaInicio
        if (fin != null && fin.isBefore(inicio)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La fecha de fin no puede ser anterior a la fecha de inicio."));
        }

        if (fin != null && edicionFeriaRepository.existeSolapamiento(edicion.getFeria().getId(), inicio, fin, id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las fechas se solapan con otra edición activa de esta feria"));
        }

        edicion.setNombreEdicion(nombreEdicion);
        edicion.setFechaInicio(inicio);
        edicion.setFechaFinal(fin);
        edicion.setHoraInicio(LocalTime.parse(horaInicio));
        edicion.setHoraFin(LocalTime.parse(horaFin));
        edicion.setCapacidad(capacidad);

        if (mapa != null && !mapa.isEmpty()) {
            Map<String, String> resultado = cloudinaryService.reemplazarImagen(mapa, edicion.getMapaPublicId());
            edicion.setMapaUrl(resultado.get("url"));
            edicion.setMapaPublicId(resultado.get("public_id"));
        }

        EdicionFeria edicionActualizada = edicionFeriaRepository.save(edicion);
        return ResponseEntity.ok(new EdicionFeriaDTO(edicionActualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEdicion(@PathVariable Integer id) {
        if (!participacionRepository.findByEdicionId(id).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No puedes eliminar una edición que ya tiene participantes (en espera, pendientes o confirmados)."));
        }
        edicionFeriaRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Edición eliminada con éxito"));
    }

}