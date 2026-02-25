package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.FeriaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.FeriaSelectorDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.ResenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FeriaController {

    private final FeriaRepository feriaRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    public FeriaController(FeriaRepository feriaRepository) {
        this.feriaRepository = feriaRepository;
    }

    // 🟢 MÉTODO INTERNO DE ACTUALIZACIÓN AUTOMÁTICA
    // Se ejecuta internamente para cerrar ferias que ya pasaron de fecha
    private List<Feria> obtenerFeriasActualizadas() {
        List<Feria> ferias = feriaRepository.findAll();
        LocalDate hoy = LocalDate.now();

        for (Feria feria : ferias) {
            // Si la feria está "Activa" pero su fecha final ya pasó, la desactivamos
            if ("Activa".equals(feria.getEstado()) &&
                    feria.getFechaFinal() != null &&
                    feria.getFechaFinal().isBefore(hoy)) {

                feria.setEstado("Inactiva");
                feriaRepository.save(feria);
            }
        }
        return ferias;
    }

    @GetMapping("/ferias")
    public List<FeriaDTO> getFerias() {
        // 🟢 CAMBIO: Llamamos primero a la lógica de actualización
        obtenerFeriasActualizadas();

        return feriaRepository.findByEliminadoFalse()
                .stream()
                .map(FeriaDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/ferias/{id}")
    public FeriaDTO getFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id)
                .map(feria -> {
                    FeriaDTO dto = new FeriaDTO(feria);
                    Long positivos = resenaRepository.countVotosPositivosFeria(id);
                    Long totales = resenaRepository.countTotalVotosFeria(id);
                    int porcentaje = (totales > 0) ? (int) ((positivos * 100.0) / totales) : 0;

                    dto.setPorcentajeAprobacion(porcentaje);
                    dto.setTotalVotos(totales.intValue());
                    return dto;
                })
                .orElse(null);
    }

    @GetMapping("/ferias/activas")
    public List<FeriaDTO> getFeriasActivas() {
        // 🟢 CAMBIO: Actualizamos estados antes de filtrar las activas para el público
        obtenerFeriasActualizadas();

        return feriaRepository.findByEstadoAndEliminadoFalse("Activa")
                .stream()
                .map(FeriaDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/ferias")
    public ResponseEntity<?> crearFeria(@RequestBody Feria nuevaFeria) {
        if (nuevaFeria.getNombre() == null || nuevaFeria.getNombre().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre no puede estar vacío");
        }
        if (nuevaFeria.getFechaInicio() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha de inicio es obligatoria");
        }
        if (nuevaFeria.getFechaInicio().isBefore(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha de inicio no puede ser anterior a hoy");
        }
        if (nuevaFeria.getFechaFinal() != null && nuevaFeria.getFechaFinal().isBefore(nuevaFeria.getFechaInicio())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha final no puede ser anterior a la fecha de inicio");
        }
        if (nuevaFeria.getLatitud() == null || nuevaFeria.getLongitud() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La ubicación geográfica es obligatoria.");
        }
        if (nuevaFeria.getNombre().trim().length() < 3 || nuevaFeria.getNombre().trim().length() > 75) {
            return ResponseEntity.badRequest().body("El nombre debe tener entre 3 y 75 caracteres");
        }
        if (nuevaFeria.getDescripcion() != null && nuevaFeria.getDescripcion().trim().length() > 300) {
            return ResponseEntity.badRequest().body("La descripción no puede superar los 300 caracteres");
        }

        nuevaFeria.setEstado("Activa");
        feriaRepository.save(nuevaFeria);
        return ResponseEntity.ok("Feria creada correctamente");
    }

    @PutMapping("/ferias/{id}")
    public ResponseEntity<?> actualizarFeria(@PathVariable Integer id, @RequestBody Feria feriaActualizada) {

        if (feriaActualizada.getFechaFinal() != null &&
                feriaActualizada.getFechaFinal().isBefore(feriaActualizada.getFechaInicio())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha final no puede ser anterior a la de inicio");
        }
        if (feriaActualizada.getLatitud() == null || feriaActualizada.getLongitud() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede guardar una feria sin coordenadas.");
        }
        if (feriaActualizada.getNombre().trim().length() < 3 || feriaActualizada.getNombre().trim().length() > 75) {
            return ResponseEntity.badRequest().body("El nombre debe tener entre 3 y 75 caracteres");
        }
        if (feriaActualizada.getDescripcion() != null && feriaActualizada.getDescripcion().trim().length() > 300) {
            return ResponseEntity.badRequest().body("La descripción no puede superar los 300 caracteres");
        }



        return feriaRepository.findById(id).map(feria -> {
            feria.setNombre(feriaActualizada.getNombre());
            feria.setLugar(feriaActualizada.getLugar());
            feria.setDescripcion(feriaActualizada.getDescripcion());
            feria.setFechaInicio(feriaActualizada.getFechaInicio());
            feria.setFechaFinal(feriaActualizada.getFechaFinal());
            feria.setImagenUrl(feriaActualizada.getImagenUrl());
            feria.setLatitud(feriaActualizada.getLatitud());
            feria.setLongitud(feriaActualizada.getLongitud());
            feriaRepository.save(feria);
            return ResponseEntity.ok("Feria actualizada correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/ferias/{id}/baja")
    public ResponseEntity<?> darDeBaja(@PathVariable Integer id) {
        return feriaRepository.findById(id).map(feria -> {
            feria.setEstado("Inactiva");
            feriaRepository.save(feria);
            return ResponseEntity.ok("Feria dada de baja");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/ferias/{id}/eliminar")
    public ResponseEntity<?> eliminarFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id).map(feria -> {
            feria.setEliminado(true);
            feria.setEstado("Inactiva");
            feriaRepository.save(feria);
            return ResponseEntity.ok("Feria eliminada correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/ferias/{id}/activar")
    public ResponseEntity<?> activarFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id).map(feria -> {
            // 🟢 VALIDACIÓN: No permitir activar si la fecha final ya pasó
            if (feria.getFechaFinal() != null && feria.getFechaFinal().isBefore(LocalDate.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se puede activar una feria cuya fecha de finalización ya pasó.");
            }
            feria.setEstado("Activa");
            feriaRepository.save(feria);
            return ResponseEntity.ok("Feria activada correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ferias/lista-select")
    public ResponseEntity<List<FeriaSelectorDTO>> getFeriasParaSelector() {
        obtenerFeriasActualizadas(); // Opcional: Actualizar antes de listar
        List<FeriaSelectorDTO> ferias = feriaRepository.findByEliminadoFalse()
                .stream()
                .map(FeriaSelectorDTO::new)
                .toList();
        return ResponseEntity.ok(ferias);
    }
}