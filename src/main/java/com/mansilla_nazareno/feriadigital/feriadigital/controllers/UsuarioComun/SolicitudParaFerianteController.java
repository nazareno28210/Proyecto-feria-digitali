package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun.SolicitudParaFerianteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.SolicitudPendienteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.RechazoSolicitudDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.SolicitudParaFeriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.EstadoSolicitud;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.SolicitudParaFerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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

    @Autowired
    private EmailService emailService;


    // 1️⃣ CREAR / REENVIAR SOLICITUD (Usuario envía formulario)
    @PostMapping("/crear/{idUsuario}")
    public ResponseEntity<?> crearSolicitud(@PathVariable int idUsuario, @RequestBody SolicitudParaFerianteDTO dto) {
        System.out.println("📩 LLEGÓ SOLICITUD al backend!");
        System.out.println("➡️ ID usuario: " + idUsuario);

        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);

        if (usuario == null) {return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");}
        if (usuario.getTipoUsuario() == TipoUsuario.FERIANTE) {return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya es feriante");}

        // VALIDACIÓN DE TELÉFONO
        String telefono = dto.getTelefono();
        String telefonoRegex = "^[0-9\\s+\\-()]*$";

        if (telefono == null || telefono.trim().isEmpty() || !telefono.matches(telefonoRegex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El teléfono solo puede contener números y símbolos válidos.");
        }

        // LÓGICA DE EMAIL OPCIONAL
        String emailEmprendimiento = dto.getEmailEmprendimiento();
        if (emailEmprendimiento == null || emailEmprendimiento.trim().isEmpty()) {
            emailEmprendimiento = usuario.getEmail();
        }

        Optional<SolicitudParaFeriante> solicitudExistenteOpt = solicitudRepository.findByUsuario(usuario);

        if (solicitudExistenteOpt.isPresent()) {
            SolicitudParaFeriante solicitudExistente = solicitudExistenteOpt.get();

            if (solicitudExistente.getEstado() == EstadoSolicitud.PENDIENTE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ya tienes una solicitud pendiente de revisión.");
            }

            // Si fue RECHAZADA previamente, la actualizamos para volver a enviarla como PENDIENTE
            solicitudExistente.setNombreEmprendimiento(dto.getNombreEmprendimiento());
            solicitudExistente.setDescripcion(dto.getDescripcion());
            solicitudExistente.setTelefono(telefono);
            solicitudExistente.setEmailEmprendimiento(emailEmprendimiento);
            solicitudExistente.setEstado(EstadoSolicitud.PENDIENTE);
            solicitudExistente.setMotivoRechazo(null);

            solicitudRepository.save(solicitudExistente);
            return ResponseEntity.status(HttpStatus.CREATED).body("Solicitud reenviada correctamente.");
        }

        // Si no existía solicitud previa, creamos una nueva
        SolicitudParaFeriante nuevaSolicitud = new SolicitudParaFeriante(
                usuario,
                dto.getNombreEmprendimiento(),
                dto.getDescripcion(),
                telefono,
                emailEmprendimiento
        );

        solicitudRepository.save(nuevaSolicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body("Solicitud enviada correctamente.");
    }

    // 2️⃣ VER PENDIENTES (Administrador ve la lista de solicitudes PENDIENTES)
    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudPendienteDTO>> obtenerPendientes() {
        List<SolicitudPendienteDTO> pendientesDTO = solicitudRepository.findAll()
                .stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE) // Solo PENDIENTES
                .map(SolicitudPendienteDTO::new)
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
        if (solicitud.isAprobada() || solicitud.getEstado() == EstadoSolicitud.APROBADA) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Esta solicitud ya fue aprobada anteriormente");
        }

        Usuario usuario = solicitud.getUsuario();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("La solicitud no tiene un usuario válido asociado.");
        }

        // Cambiar rol a FERIANTE
        usuario.setTipoUsuario(TipoUsuario.FERIANTE);
        usuarioRepository.save(usuario);

        // 1. Crear Feriante
        Feriante nuevoFeriante = new Feriante(
                solicitud.getTelefono(),
                solicitud.getEmailEmprendimiento(),
                EstadoUsuario.ACTIVO
        );
        nuevoFeriante.setUsuario(solicitud.getUsuario());
        ferianteRepository.save(nuevoFeriante);

        // 2. Crear Stand automáticamente
        Stand nuevoStand = new Stand();
        nuevoStand.setNombre(solicitud.getNombreEmprendimiento());
        nuevoStand.setDescripcion(solicitud.getDescripcion());
        nuevoStand.setFeriante(nuevoFeriante);
        standRepository.save(nuevoStand);

        // 3. Marcar solicitud como APROBADA (Persiste en BD)
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setAprobada(true);
        solicitud.setMotivoRechazo(null);
        solicitudRepository.save(solicitud);

        // 4. Enviar email de notificación de aprobación al usuario
        emailService.enviarEmailAprobacionSolicitud(
                usuario.getEmail(),
                usuario.getNombre(),
                solicitud.getNombreEmprendimiento()
        );

        return ResponseEntity.ok("Solicitud aprobada: El usuario ahora es Feriante y tiene su Stand listo.");
    }

    // 4️⃣ RECHAZAR SOLICITUD (Con motivo, notificación por email y sin eliminar de la BD)
    @PostMapping("/rechazar/{idSolicitud}")
    @Transactional
    public ResponseEntity<?> rechazarSolicitud(@PathVariable int idSolicitud, @RequestBody(required = false) RechazoSolicitudDTO dto) {
        SolicitudParaFeriante solicitud = solicitudRepository.findById(idSolicitud).orElse(null);

        if (solicitud == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitud no encontrada");
        }

        if (solicitud.isAprobada() || solicitud.getEstado() == EstadoSolicitud.APROBADA) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede rechazar una solicitud ya aprobada");
        }

        String motivo = (dto != null) ? dto.getMotivoRechazo() : null;
        if (motivo == null || motivo.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Debes indicar un motivo de rechazo para la solicitud.");
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setAprobada(false);
        solicitud.setMotivoRechazo(motivo.trim());

        solicitudRepository.save(solicitud);

        // Enviar email de notificación de rechazo con el motivo indicado
        if (solicitud.getUsuario() != null) {
            emailService.enviarEmailRechazoSolicitud(
                    solicitud.getUsuario().getEmail(),
                    solicitud.getUsuario().getNombre(),
                    solicitud.getNombreEmprendimiento(),
                    motivo.trim()
            );
        }

        return ResponseEntity.ok("Solicitud rechazada correctamente.");
    }

}
