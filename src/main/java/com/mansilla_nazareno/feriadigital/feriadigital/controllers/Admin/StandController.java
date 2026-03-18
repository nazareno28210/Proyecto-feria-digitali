package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.configurations.CloudinaryDefaults;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.StandDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.StandUpdateDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoParticipacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.ResenaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ParticipacionRepository participacionRepository;

    public StandController(
            StandRepository standRepository,
            UsuarioRepository usuarioRepository,
            FerianteRepository ferianteRepository,
            FeriaRepository feriaRepository,
            CloudinaryService cloudinaryService,
            ParticipacionRepository participacionRepository

    ){

        this.standRepository= standRepository;
        this.usuarioRepository = usuarioRepository;
        this.ferianteRepository = ferianteRepository;
        this.feriaRepository =feriaRepository;
        this.cloudinaryService = cloudinaryService;
        this.participacionRepository=participacionRepository;

    }
    @GetMapping("/stands")
    public List<StandDTO> getStands(){
        return standRepository.findAll()
                .stream()
                .map(stand-> new StandDTO(stand))
                .toList();
    }

    // Para que el público solo vea stands abiertos
    /*
    @GetMapping("/stands/activos")
    public List<StandDTO> getStandsActivos() {
        return standRepository.findByActivoTrue()
                .stream()
                .map(StandDTO::new)
                .toList();
    }
*/
    @GetMapping("/stands/{id}")
    public StandDTO getStandDTO(@PathVariable Integer id) {
        return standRepository.findById(id)
                .map(stand -> {
                    // Calculamos promedio y cantidad desde la DB
                    Double promedio = resenaRepository.getPromedioPorStand(id);
                    Long cantidad = resenaRepository.getCantidadResenasPorStand(id);

                    StandDTO dto = new StandDTO(stand);
                    dto.setPromedioEstrellas(promedio != null ? promedio : 0.0);
                    dto.setCantidadResenas(cantidad != null ? cantidad.intValue() : 0);
                    return dto;
                })
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


    // --- ACTUALIZAR ASIGNACIÓN ---
    @PatchMapping("/stands/{standId}/asignar-feria/{feriaId}")
    public ResponseEntity<?> asignarStandAFeria(@PathVariable Integer standId, @PathVariable Integer feriaId) {
        Stand stand = standRepository.findById(standId).orElse(null);
        Feria feria = feriaRepository.findById(feriaId).orElse(null);

        if (stand == null) return new ResponseEntity<>("Stand no encontrado", HttpStatus.NOT_FOUND);
        if (feria == null) return new ResponseEntity<>("Feria no encontrada", HttpStatus.NOT_FOUND);

        if (!stand.isActivo()) {
            return new ResponseEntity<>("No se puede asignar un stand desactivado", HttpStatus.BAD_REQUEST);
        }

        // Validar si ya existe la participación para evitar duplicados
        if (participacionRepository.existsByFeriaIdAndStandId(feriaId, standId)) {
            return new ResponseEntity<>("El stand ya está asignado a esta feria", HttpStatus.BAD_REQUEST);
        }

        // Crear la nueva participación (Estado PENDIENTE por defecto o CONFIRMADO según tu flujo)
        Participacion participacion = new Participacion(feria, stand, null, EstadoParticipacion.CONFIRMADO);
        participacionRepository.save(participacion);

        return new ResponseEntity<>("Participación registrada correctamente", HttpStatus.OK);
    }

    // ▼▼▼ NUEVO ENDPOINT PARA DESASIGNAR ▼▼▼
    // --- ACTUALIZAR DESASIGNACIÓN ---
    @DeleteMapping("/stands/{standId}/desasignar-feria/{feriaId}")
    public ResponseEntity<?> desasignarStandDeFeria(@PathVariable Integer standId, @PathVariable Integer feriaId) {
        // Buscamos la participación específica entre ese stand y esa feria
        return participacionRepository.findByFeriaId(feriaId).stream()
                .filter(p -> p.getStand().getId() == standId)
                .findFirst()
                .map(p -> {
                    participacionRepository.delete(p);
                    return new ResponseEntity<>("Stand desasignado de la feria", HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>("No se encontró la participación", HttpStatus.NOT_FOUND));
    }




    // ▼▼▼cambiar imagen ▼▼▼
    @PostMapping("/stands/mi-stand/imagen")
    public ResponseEntity<?> cambiarImagenStand(
            Authentication authentication,
            @RequestParam("imagen") MultipartFile imagen
    ) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);
        Stand stand = standRepository.findByFeriante(feriante);

        if (stand == null) {
            return new ResponseEntity<>("Stand no encontrado", HttpStatus.NOT_FOUND);
        }

        Map<String, String> resultado =
                cloudinaryService.reemplazarImagen(
                        imagen,
                        stand.getImagenPublicId()
                );

        stand.setImagenUrl(resultado.get("url"));
        stand.setImagenPublicId(resultado.get("public_id"));

        standRepository.save(stand);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Imagen del stand actualizada correctamente",
                "url", stand.getImagenUrl()
        ));
    }

    @DeleteMapping("/stands/mi-stand/imagen")
    public ResponseEntity<?> borrarImagenStand(Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);
        Stand stand = standRepository.findByFeriante(feriante);

        if (stand == null) {
            return new ResponseEntity<>("Stand no encontrado", HttpStatus.NOT_FOUND);
        }

        // ⚠️ Solo borrar si NO es la imagen default
        if (stand.getImagenPublicId() != null &&
                !stand.getImagenPublicId().equals(CloudinaryDefaults.STAND_DEFAULT_PUBLIC_ID)) {

            cloudinaryService.borrarImagen(stand.getImagenPublicId());
        }

        // 👇 Volver a la default
        stand.setImagenPublicId(CloudinaryDefaults.STAND_DEFAULT_PUBLIC_ID);
        stand.setImagenUrl(CloudinaryDefaults.STAND_DEFAULT_URL);

        standRepository.save(stand);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Imagen del stand restaurada a la predeterminada"
        ));
    }

    @PatchMapping("/stands/mi-stand/toggle-activo")
    public ResponseEntity<?> toggleActivo(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);
        Stand stand = standRepository.findByFeriante(feriante);

        if (stand == null) {
            return new ResponseEntity<>("Stand no encontrado", HttpStatus.NOT_FOUND);
        }

        stand.setActivo(!stand.isActivo()); // Cambia de true a false o viceversa
        standRepository.save(stand);

        String estado = stand.isActivo() ? "Abierto" : "Cerrado";
        return ResponseEntity.ok(Map.of("mensaje", "Tu stand ahora está " + estado, "activo", stand.isActivo()));
    }


}
