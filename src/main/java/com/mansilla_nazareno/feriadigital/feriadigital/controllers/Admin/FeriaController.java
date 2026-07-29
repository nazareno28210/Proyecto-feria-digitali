package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.FeriaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.FeriaSelectorDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.ResenaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private EdicionFeriaRepository edicionFeriaRepository;

    @Autowired
    private ParticipacionRepository participacionRepository;

    public FeriaController(FeriaRepository feriaRepository) {
        this.feriaRepository = feriaRepository;
    }

    // 🟢 1. Obtener todas las ferias plantilla (Para el listado del Admin)
    @GetMapping("/ferias")
    public List<FeriaDTO> getFerias() {
        return feriaRepository.findByEliminadoFalse()
                .stream()
                .map(FeriaDTO::new)
                .collect(Collectors.toList());
    }

    // 🟢 2. Obtener una plantilla específica con sus estadísticas de reseñas
    @GetMapping("/ferias/{id}")
    public FeriaDTO getFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id)
                .map(feria -> {
                    FeriaDTO dto = new FeriaDTO(feria);

                    // Mantenemos tus estadísticas de reseñas vinculadas a la plantilla base
                    Long positivos = resenaRepository.countVotosPositivosFeria(id);
                    Long totales = resenaRepository.countTotalVotosFeria(id);
                    int porcentaje = (totales != null && totales > 0) ? (int) ((positivos * 100.0) / totales) : 0;

                    dto.setPorcentajeAprobacion(porcentaje);
                    dto.setTotalVotos(totales != null ? totales.intValue() : 0);

                    return dto;
                })
                .orElse(null);
    }

// 🟢 3. Crear una nueva plantilla de feria (Corregido para devolver el objeto)
    @PostMapping("/ferias")
    public ResponseEntity<?> crearFeria(
            @RequestParam("nombre") String nombre,
            @RequestParam("lugar") String lugar,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam(value = "capacidad", required = false) Integer capacidad,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre no puede estar vacío");
        }
        if (nombre.trim().length() < 3 || nombre.trim().length() > 75) {
            return ResponseEntity.badRequest().body("El nombre debe tener entre 3 y 75 caracteres");
        }
        if (latitud == null || longitud == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La ubicación geográfica es obligatoria.");
        }
        if (descripcion != null && descripcion.trim().length() > 300) {
            return ResponseEntity.badRequest().body("La descripción no puede superar los 300 caracteres");
        }
        if (capacidad != null && capacidad <= 0) {
            return ResponseEntity.badRequest().body("La capacidad de stands debe ser mayor a 0");
        }

        try {
            Feria nuevaFeria = new Feria();
            nuevaFeria.setNombre(nombre);
            nuevaFeria.setLugar(lugar);
            nuevaFeria.setDescripcion(descripcion);
            nuevaFeria.setLatitud(latitud);
            nuevaFeria.setLongitud(longitud);
            nuevaFeria.setCapacidad(capacidad);

            if (imagen != null && !imagen.isEmpty()) {
                Map<String, String> result = cloudinaryService.subirImagen(imagen);
                nuevaFeria.setImagenUrl(result.get("url"));
            }

            // 🟢 GUARDAMOS Y DEVOLVEMOS EL DTO (Esto arregla el frontend)
            Feria feriaGuardada = feriaRepository.save(nuevaFeria);
            return ResponseEntity.ok(new FeriaDTO(feriaGuardada));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la imagen");
        }
    }

    // 🟢 4. Actualizar los datos estructurales del molde
    @PutMapping("/ferias/{id}")
    public ResponseEntity<?> actualizarFeria(
            @PathVariable Integer id,
            @RequestParam("nombre") String nombre,
            @RequestParam("lugar") String lugar,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam(value = "capacidad", required = false) Integer capacidad,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        if (latitud == null || longitud == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede guardar una feria sin coordenadas.");
        }
        if (nombre.trim().length() < 3 || nombre.trim().length() > 75) {
            return ResponseEntity.badRequest().body("El nombre debe tener entre 3 y 75 caracteres");
        }
        if (descripcion != null && descripcion.trim().length() > 300) {
            return ResponseEntity.badRequest().body("La descripción no puede superar los 300 caracteres");
        }
        if (capacidad != null && capacidad <= 0) {
            return ResponseEntity.badRequest().body("La capacidad de stands debe ser mayor a 0");
        }

        return feriaRepository.findById(id).map(feria -> {
            try {
                if (capacidad != null && feria.getCapacidad() != null && capacidad < feria.getCapacidad()) {
                    List<EdicionFeria> edicionesActivas = edicionFeriaRepository.findByFeriaId(id).stream()
                            .filter(e -> "ACTIVA".equalsIgnoreCase(e.getEstado()))
                            .collect(Collectors.toList());

                    for (EdicionFeria edicion : edicionesActivas) {
                        long ocupantes = participacionRepository.findByEdicionId(edicion.getId()).stream()
                                .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO || p.getEstado() == EstadoParticipacion.PENDIENTE)
                                .count();
                        if (capacidad < ocupantes) {
                            return ResponseEntity.badRequest().body(Map.of("error", "No puedes reducir la capacidad. Ya existen ediciones con más feriantes activos que el nuevo límite."));
                        }
                    }
                }

                feria.setNombre(nombre);
                feria.setLugar(lugar);
                feria.setDescripcion(descripcion);
                feria.setLatitud(latitud);
                feria.setLongitud(longitud);
                feria.setCapacidad(capacidad);

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
                return ResponseEntity.ok("Feria plantilla actualizada correctamente");

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la imagen");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // 🟢 5. Borrado lógico de la plantilla (Va a la papelera)
    @PutMapping("/ferias/{id}/eliminar")
    public ResponseEntity<?> eliminarFeria(@PathVariable Integer id) {
        return feriaRepository.findById(id).map(feria -> {
            feria.setEliminado(true);
            feriaRepository.save(feria);
            return ResponseEntity.ok("Feria plantilla enviada a la papelera correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }

    // 🟢 6. Endpoint para alimentar el select cuando el Admin crea una Edición Nueva
    @GetMapping("/ferias/lista-select")
    public ResponseEntity<List<FeriaSelectorDTO>> getFeriasParaSelector() {
        List<FeriaSelectorDTO> ferias = feriaRepository.findByEliminadoFalse()
                .stream()
                .map(FeriaSelectorDTO::new)
                .toList();
        return ResponseEntity.ok(ferias);
    }
}