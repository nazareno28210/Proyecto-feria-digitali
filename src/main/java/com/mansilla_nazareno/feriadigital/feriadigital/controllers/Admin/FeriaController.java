package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.FeriaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.FeriaSelectorDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.StandDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoParticipacion;
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

    public FeriaController(FeriaRepository feriaRepository) {this.feriaRepository = feriaRepository;}

    @GetMapping("/ferias")
    public List<FeriaDTO> getFerias() {
        // 🟢 CAMBIO: Usamos findByEliminadoFalse() para no mostrar la "papelera" al Admin
        return feriaRepository.findByEliminadoFalse()
                .stream()
                .map(feria -> new FeriaDTO(feria))
                .collect(Collectors.toList());
    }

    @GetMapping("/ferias/{id}")
    public ResponseEntity<FeriaDTO> getFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id)
                .map(feria -> {
                    FeriaDTO dto = new FeriaDTO(feria);

                    // Mapear stands confirmados explícitamente
                    List<StandDTO> stands = feria.getParticipaciones().stream()
                            .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO)
                            .map(p -> new StandDTO(p.getStand()))
                            .toList();
                    dto.setStands(stands);

                    // Cálculos de votos
                    Long positivos = resenaRepository.countVotosPositivosFeria(id);
                    Long totales = resenaRepository.countTotalVotosFeria(id);
                    int porcentaje = (totales > 0) ? (int) ((positivos * 100.0) / totales) : 0;

                    dto.setPorcentajeAprobacion(porcentaje);
                    dto.setTotalVotos(totales.intValue());

                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //obtener ferias activas
    @GetMapping("/ferias/activas")
    public List<FeriaDTO> getFeriasActivas() {
        return feriaRepository.findByEstadoAndEliminadoFalse("Activa")
                .stream()
                .map(FeriaDTO::new)
                .collect(Collectors.toList());
    }

    //crear feria
    @PostMapping("/ferias")
    public ResponseEntity<?> crearFeria(@RequestBody Feria nuevaFeria) {

        // 🟢 INICIO DE VALIDACIÓN DE BACKEND 🟢

        // 1. Validar que no falten datos (aunque el frontend ya lo hizo)
        if (nuevaFeria.getNombre() == null || nuevaFeria.getNombre().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre no puede estar vacío");
        }
        if (nuevaFeria.getFechaInicio() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha de inicio es obligatoria");
        }

        // 2. Validar la lógica de fechas (la misma que en el JS)
        if (nuevaFeria.getFechaInicio().isBefore(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha de inicio no puede ser anterior a hoy");
        }

        if (nuevaFeria.getFechaFinal() != null && nuevaFeria.getFechaFinal().isBefore(nuevaFeria.getFechaInicio())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha final no puede ser anterior a la fecha de inicio");
        }

        // 🟢 FIN DE VALIDACIÓN 🟢

        // Si todo está OK, se procede a guardar:
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

//  ELIMINAR FERIA (BORRADO LÓGICO)
    @PutMapping("/ferias/{id}/eliminar")
    public ResponseEntity<?> eliminarFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id).map(feria -> {
            feria.setEliminado(true);
            feria.setEstado("Inactiva"); // Al eliminarla, también la desactivamos para el público
            feriaRepository.save(feria);
            return ResponseEntity.ok("Feria eliminada de la vista correctamente");
        }).orElse(ResponseEntity.notFound().build());
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
    // Lista de ferias para el selector (id + nombre) - SOLO las que no estén eliminadas
    @GetMapping("/ferias/lista-select")
    public ResponseEntity<List<FeriaSelectorDTO>> getFeriasParaSelector() {
        List<FeriaSelectorDTO> ferias = feriaRepository.findByEliminadoFalse()
                .stream()
                .map(FeriaSelectorDTO::new)
                .toList();
        return ResponseEntity.ok(ferias);
    }
}
