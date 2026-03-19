package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.FeriaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.FeriaSelectorDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.StandDTO; // 🟢 Importado para la lógica de tu amigo
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoParticipacion; // 🟢 Importado para la lógica de tu amigo
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.ResenaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FeriaController {

    private final FeriaRepository feriaRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public FeriaController(FeriaRepository feriaRepository) {
        this.feriaRepository = feriaRepository;
    }

    private List<Feria> obtenerFeriasActualizadas() {
        List<Feria> ferias = feriaRepository.findAll();
        LocalDate hoy = LocalDate.now();

        for (Feria feria : ferias) {
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

                    // 🟢 LÓGICA DE TU AMIGO (Integrada): Mapear stands confirmados explícitamente desde Participaciones
                    if (feria.getParticipaciones() != null) {
                        List<StandDTO> standsConfirmados = feria.getParticipaciones().stream()
                                .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO)
                                .map(p -> new StandDTO(p.getStand()))
                                .collect(Collectors.toList());
                        dto.setStands(standsConfirmados);
                    }

                    // 🟢 TU LÓGICA (Mantenida): Cálculos de reseñas
                    Long positivos = resenaRepository.countVotosPositivosFeria(id);
                    Long totales = resenaRepository.countTotalVotosFeria(id);
                    int porcentaje = (totales != null && totales > 0) ? (int) ((positivos * 100.0) / totales) : 0;

                    dto.setPorcentajeAprobacion(porcentaje);
                    dto.setTotalVotos(totales != null ? totales.intValue() : 0);

                    return dto;
                })
                .orElse(null);
    }

    @GetMapping("/ferias/activas")
    public List<FeriaDTO> getFeriasActivas() {
        obtenerFeriasActualizadas();

        return feriaRepository.findByEstadoAndEliminadoFalse("Activa")
                .stream()
                .map(FeriaDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/ferias")
    public ResponseEntity<?> crearFeria(
            @RequestParam("nombre") String nombre,
            @RequestParam("lugar") String lugar,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam("fechaFinal") String fechaFinal,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre no puede estar vacío");
        }
        if (nombre.trim().length() < 3 || nombre.trim().length() > 75) {
            return ResponseEntity.badRequest().body("El nombre debe tener entre 3 y 75 caracteres");
        }

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = (fechaFinal != null && !fechaFinal.isEmpty()) ? LocalDate.parse(fechaFinal) : null;

        if (inicio.isBefore(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha de inicio no puede ser anterior a hoy");
        }
        if (fin != null && fin.isBefore(inicio)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha final no puede ser anterior a la de inicio");
        }
        if (latitud == null || longitud == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La ubicación geográfica es obligatoria.");
        }
        if (descripcion != null && descripcion.trim().length() > 300) {
            return ResponseEntity.badRequest().body("La descripción no puede superar los 300 caracteres");
        }

        try {
            Feria nuevaFeria = new Feria();
            nuevaFeria.setNombre(nombre);
            nuevaFeria.setLugar(lugar);
            nuevaFeria.setDescripcion(descripcion);
            nuevaFeria.setFechaInicio(inicio);
            nuevaFeria.setFechaFinal(fin);
            nuevaFeria.setLatitud(latitud);
            nuevaFeria.setLongitud(longitud);
            nuevaFeria.setEstado("Activa");

            if (imagen != null && !imagen.isEmpty()) {
                Map<String, String> result = cloudinaryService.subirImagen(imagen);
                nuevaFeria.setImagenUrl(result.get("url"));
            }

            feriaRepository.save(nuevaFeria);
            return ResponseEntity.ok("Feria creada correctamente");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la imagen");
        }
    }


    @PutMapping("/ferias/{id}")
    public ResponseEntity<?> actualizarFeria(
            @PathVariable Integer id,
            @RequestParam("nombre") String nombre,
            @RequestParam("lugar") String lugar,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam("fechaFinal") String fechaFinal,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = (fechaFinal != null && !fechaFinal.isEmpty()) ? LocalDate.parse(fechaFinal) : null;

        if (fin != null && fin.isBefore(inicio)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha final no puede ser anterior a la de inicio");
        }
        if (latitud == null || longitud == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede guardar una feria sin coordenadas.");
        }
        if (nombre.trim().length() < 3 || nombre.trim().length() > 75) {
            return ResponseEntity.badRequest().body("El nombre debe tener entre 3 y 75 caracteres");
        }
        if (descripcion != null && descripcion.trim().length() > 300) {
            return ResponseEntity.badRequest().body("La descripción no puede superar los 300 caracteres");
        }

        return feriaRepository.findById(id).map(feria -> {
            try {
                feria.setNombre(nombre);
                feria.setLugar(lugar);
                feria.setDescripcion(descripcion);
                feria.setFechaInicio(inicio);
                feria.setFechaFinal(fin);
                feria.setLatitud(latitud);
                feria.setLongitud(longitud);

                if (imagen != null && !imagen.isEmpty()) {
                    String urlVieja = feria.getImagenUrl();
                    String publicIdViejo = null;

                    if (urlVieja != null && urlVieja.contains("upload/")) {
                        publicIdViejo = urlVieja.substring(urlVieja.lastIndexOf("/") + 1, urlVieja.lastIndexOf("."));
                    }

                    Map<String, String> result = cloudinaryService.reemplazarImagen(imagen, publicIdViejo);
                    feria.setImagenUrl(result.get("url"));
                }

                feriaRepository.save(feria);
                return ResponseEntity.ok("Feria actualizada correctamente");

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la imagen");
            }
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
        obtenerFeriasActualizadas();
        List<FeriaSelectorDTO> ferias = feriaRepository.findByEliminadoFalse()
                .stream()
                .map(FeriaSelectorDTO::new)
                .toList();
        return ResponseEntity.ok(ferias);
    }
}