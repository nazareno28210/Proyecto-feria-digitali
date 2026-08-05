package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.EdicionFeriaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoEdicion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoEspacio;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EspacioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(EdicionFeriaController.class);

    @Autowired
    private EdicionFeriaRepository edicionFeriaRepository;

    @Autowired
    private FeriaRepository feriaRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ParticipacionRepository participacionRepository;

    @Autowired
    private EspacioRepository espacioRepository;

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
        List<EdicionFeria> ediciones = edicionFeriaRepository.findByEstado(EstadoEdicion.ACTIVA);
        ediciones.addAll(edicionFeriaRepository.findByEstado(EstadoEdicion.PROXIMA));
        List<EdicionFeriaDTO> activas = ediciones.stream()
                .map(edicion -> {
                    EdicionFeriaDTO dto = new EdicionFeriaDTO(edicion);
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

        logger.info("Solicitud para crear nueva edición '{}' para feriaId {}", nombreEdicion, feriaId);

        Feria feria = feriaRepository.findById(feriaId).orElse(null);
        if (feria == null) {
            logger.warn("Falla al crear edición: Feria base con id {} no existe", feriaId);
            return ResponseEntity.badRequest().body(Map.of("error", "La feria base seleccionada no existe"));
        }

        if (capacidad != null && capacidad <= 0) {
            logger.warn("Falla al crear edición: Capacidad no válida ({})", capacidad);
            return ResponseEntity.badRequest().body(Map.of("error", "La capacidad de stands debe ser mayor a 0"));
        }

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = (fechaFinal != null && !fechaFinal.isEmpty()) ? LocalDate.parse(fechaFinal) : null;

        if (fin != null && fin.isBefore(inicio)) {
            logger.warn("Falla al crear edición: fechaFin ({}) es anterior a fechaInicio ({})", fin, inicio);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La fecha de fin no puede ser anterior a la fecha de inicio."));
        }

        if (fin != null && edicionFeriaRepository.existeSolapamiento(feria.getId(), inicio, fin, null)) {
            logger.warn("Falla al crear edición: Solapamiento detectado para feriaId {} entre {} y {}", feria.getId(), inicio, fin);
            return ResponseEntity.badRequest().body(Map.of("error", "Las fechas se solapan con otra edición activa de esta feria"));
        }

        EdicionFeria nuevaEdicion = new EdicionFeria();
        nuevaEdicion.setFeria(feria);
        nuevaEdicion.setNombreEdicion(nombreEdicion);
        nuevaEdicion.setFechaInicio(inicio);
        nuevaEdicion.setFechaFinal(fin);
        nuevaEdicion.setHoraInicio(LocalTime.parse(horaInicio));
        nuevaEdicion.setHoraFin(LocalTime.parse(horaFin));
        nuevaEdicion.setEstado(EstadoEdicion.PROXIMA); // 🟢 Nace por defecto como PROXIMA
        nuevaEdicion.setCapacidad(capacidad);

        if (mapa != null && !mapa.isEmpty()) {
            Map<String, String> resultado = cloudinaryService.subirImagen(mapa);
            nuevaEdicion.setMapaUrl(resultado.get("url"));
            nuevaEdicion.setMapaPublicId(resultado.get("public_id"));
        }

        EdicionFeria guardada = edicionFeriaRepository.save(nuevaEdicion);
        logger.info("Edición creada exitosamente con id {} y estado PROXIMA", guardada.getId());
        return ResponseEntity.ok(new EdicionFeriaDTO(guardada));
    }

    // Cambiar estado (Para finalizar o cancelar un evento)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam String nuevoEstado) {
        logger.info("Solicitud para cambiar estado de edicion id {} a {}", id, nuevoEstado);
        EdicionFeria edicion = edicionFeriaRepository.findById(id).orElse(null);
        if (edicion == null) {
            logger.warn("Edición id {} no encontrada al intentar cambiar estado", id);
            return ResponseEntity.notFound().build();
        }

        try {
            EstadoEdicion estadoEnum = EstadoEdicion.valueOf(nuevoEstado.toUpperCase());
            edicion.setEstado(estadoEnum);
            edicionFeriaRepository.save(edicion);
            logger.info("Estado de edición id {} actualizado correctamente a {}", id, estadoEnum);

            if (estadoEnum == EstadoEdicion.ELIMINADO && edicion.getFeria() != null) {
                Feria feriaBase = edicion.getFeria();
                List<EdicionFeria> todasLasEdiciones = edicionFeriaRepository.findByFeriaId(feriaBase.getId());
                boolean todasEliminadas = todasLasEdiciones.stream()
                        .allMatch(e -> e.getEstado() == EstadoEdicion.ELIMINADO);
                if (todasEliminadas) {
                    feriaBase.setEliminado(true);
                    feriaRepository.save(feriaBase);
                    logger.info("Feria base id {} marcada como eliminada al estar todas sus ediciones eliminadas", feriaBase.getId());
                }
            }

            return ResponseEntity.ok(Map.of("mensaje", "Estado de la edición actualizado con éxito"));
        } catch (IllegalArgumentException e) {
            logger.warn("Estado inválido proporcionado: {}", nuevoEstado);
            return ResponseEntity.badRequest().body(Map.of("error", "Estado de edición no válido: " + nuevoEstado));
        }
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

        if (capacidad != null) {
            long standsCreados = espacioRepository.findByEdicionId(id).stream()
                    .filter(e -> e.getEstado() != EstadoEspacio.ELIMINADO)
                    .count();
            if (capacidad < standsCreados) {
                logger.warn("Reducción de cupo rechazada para edicionId {}: nueva capacidad {} < stands creados {}", id, capacidad, standsCreados);
                return ResponseEntity.badRequest().body(Map.of("error", "No puedes reducir el cupo a " + capacidad + " porque ya existen " + standsCreados + " stands creados."));
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
        EdicionFeria edicion = edicionFeriaRepository.findById(id).orElse(null);
        if (edicion == null) {
            return ResponseEntity.notFound().build();
        }
        edicion.setEstado(EstadoEdicion.ELIMINADO);
        edicionFeriaRepository.save(edicion);
        if (edicion.getFeria() != null) {
            Feria feriaBase = edicion.getFeria();
            List<EdicionFeria> todasLasEdiciones = edicionFeriaRepository.findByFeriaId(feriaBase.getId());
            boolean todasEliminadas = todasLasEdiciones.stream()
                    .allMatch(e -> e.getEstado() == EstadoEdicion.ELIMINADO);
            if (todasEliminadas) {
                feriaBase.setEliminado(true);
                feriaRepository.save(feriaBase);
                logger.info("Feria base id {} marcada como eliminada al estar todas sus ediciones eliminadas", feriaBase.getId());
            }
        }
        return ResponseEntity.ok(Map.of("mensaje", "Edición eliminada con éxito"));
    }

}