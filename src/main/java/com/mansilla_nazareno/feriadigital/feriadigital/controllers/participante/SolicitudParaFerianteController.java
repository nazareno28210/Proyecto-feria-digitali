package com.mansilla_nazareno.feriadigital.feriadigital.controllers.participante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.TipoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.participant.SolicitudParaFerianteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.SolicitudPendienteDTO;

import com.mansilla_nazareno.feriadigital.feriadigital.models.feria.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.participant.SolicitudParaFeriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.auth.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.fair.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.participant.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.participant.SolicitudParaFerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.auth.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudParaFerianteController {

    @Autowired
    private SolicitudParaFerianteRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FerianteRepository ferianteRepository;

    @Autowired
    private StandRepository standRepository;


    // 1️⃣ CREAR SOLICITUD (Usuario envía formulario)
    @PostMapping("/crear/{idUsuario}")
    public ResponseEntity<?> crearSolicitud(@PathVariable int idUsuario, @RequestBody SolicitudParaFerianteDTO dto) {
        System.out.println("📩 LLEGÓ SOLICITUD al backend!");
        System.out.println("➡️ ID usuario: " + idUsuario);
        System.out.println("➡️ Nombre emprendimiento: " + dto.getNombreEmprendimiento());
        System.out.println("➡️ Descripción: " + dto.getDescripcion());
        System.out.println("➡️ Teléfono: " + dto.getTelefono());
        System.out.println("➡️ Email: " + dto.getEmailEmprendimiento());

        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);

        if (usuario == null) {return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");}
        if (usuario.getTipoUsuario() == TipoUsuario.FERIANTE) {return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya es feriante");}
        if (solicitudRepository.findByUsuario(usuario).isPresent() && !solicitudRepository.findByUsuario(usuario).get().isAprobada()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ya tienes una solicitud pendiente de revisión.");
        }

        //   VALIDACIÓN DE TELÉFONO
        String telefono = dto.getTelefono();
        String telefonoRegex = "^[0-9\\s+\\-()]*$"; // Expresión regular para Java

        if (telefono == null || telefono.trim().isEmpty() || !telefono.matches(telefonoRegex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El teléfono solo puede contener números y símbolos válidos.");
        }

        // LÓGICA DE EMAIL OPCIONAL
        String emailEmprendimiento = dto.getEmailEmprendimiento();

        if (emailEmprendimiento == null || emailEmprendimiento.trim().isEmpty()) {
            // Si está vacío, asigna el email principal del usuario
            emailEmprendimiento = usuario.getEmail();
            System.out.println("➡️ Email emprendimiento vacío, asignando email de usuario: " + emailEmprendimiento);
        }

        // Creamos la entidad con los datos del DTO
        SolicitudParaFeriante solicitud = new SolicitudParaFeriante(
                usuario,
                dto.getNombreEmprendimiento(),
                dto.getDescripcion(),
                telefono, // Usamos la variable validada
                emailEmprendimiento
        );

        solicitudRepository.save(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body("Solicitud enviada correctamente.");
    }

    // 2️⃣ VER PENDIENTES (Administrador ve la lista)
    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudPendienteDTO>> obtenerPendientes() {
        List<SolicitudPendienteDTO> pendientesDTO = solicitudRepository.findAll()
                .stream()
                .filter(s -> !s.isAprobada()) // Solo las NO aprobadas
                .map(solicitud -> new SolicitudPendienteDTO(solicitud)) // Convertimos a DTO de salida
                .collect(Collectors.toList());

        return ResponseEntity.ok(pendientesDTO);
    }

    // ================================================================
    // 3️⃣ APROBAR SOLICITUD (Administrador aprueba y se crea el Feriante)
    // ================================================================
    @PostMapping("/aprobar/{idSolicitud}")
    @Transactional
    public ResponseEntity<?> aprobarSolicitud(@PathVariable int idSolicitud) {
        SolicitudParaFeriante solicitud = solicitudRepository.findById(idSolicitud).orElse(null);

        if (solicitud == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitud no encontrada");
        }
        if (solicitud.isAprobada()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Esta solicitud ya fue aprobada anteriormente");
        }
        // Obtenemos el usuario de la solicitud
        Usuario usuario = solicitud.getUsuario();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("La solicitud no tiene un usuario válido asociado.");
        }

        // ¡Cambiamos el tipo de usuario!
        usuario.setTipoUsuario(TipoUsuario.FERIANTE);
        // Guardamos el cambio en la base de datos
        usuarioRepository.save(usuario);

        // 1. Crear Feriante (IGUAL QUE ANTES)
        Feriante nuevoFeriante = new Feriante(
                solicitud.getNombreEmprendimiento(),
                solicitud.getDescripcion(),
                solicitud.getTelefono(),
                solicitud.getEmailEmprendimiento(),
                EstadoUsuario.ACTIVO
        );
        nuevoFeriante.setUsuario(solicitud.getUsuario());
        ferianteRepository.save(nuevoFeriante);

        // 2. NUEVO: Crear el Stand automáticamente
        Stand nuevoStand = new Stand();
        // Puedes usar el mismo nombre del emprendimiento para el stand inicialmente
        nuevoStand.setNombre(solicitud.getNombreEmprendimiento());
        nuevoStand.setDescripcion("Stand de " + solicitud.getNombreEmprendimiento());

        // Vinculación bidireccional importante:
        nuevoStand.setFeriante(nuevoFeriante);

        // NOTA: Aquí el stand NO tiene Feria asignada aún.
        // Ver punto 3 de mis recomendaciones más abajo.

        standRepository.save(nuevoStand);

        // 3. Marcar solicitud como aprobada (IGUAL QUE ANTES)
        solicitud.setAprobada(true);
        solicitudRepository.save(solicitud);

        return ResponseEntity.ok("Solicitud aprobada: El usuario ahora es Feriante y tiene su Stand listo.");
    }

    // 4️⃣ RECHAZAR SOLICITUD
    @PostMapping("/rechazar/{idSolicitud}")
    @Transactional
    public ResponseEntity<?> rechazarSolicitud(@PathVariable int idSolicitud) {
        SolicitudParaFeriante solicitud = solicitudRepository.findById(idSolicitud).orElse(null);

        if (solicitud == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitud no encontrada");
        }

        if (solicitud.isAprobada()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede rechazar una solicitud ya aprobada");
        }

        // Borramos la solicitud o la marcamos como rechazada (según tu preferencia)
        solicitudRepository.delete(solicitud);
        return ResponseEntity.ok("Solicitud rechazada y eliminada correctamente");
    }

}