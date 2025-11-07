package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.SolicitudParaFerianteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.SolicitudPendienteDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.*;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.*;
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

    // ================================================================
    // 1️⃣ CREAR SOLICITUD (Usuario envía formulario)
    // ================================================================
    @PostMapping("/crear/{idUsuario}")
    public ResponseEntity<?> crearSolicitud(@PathVariable int idUsuario, @RequestBody SolicitudParaFerianteDTO dto) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);

        if (usuario == null) {return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");}
        if (usuario.getTipoUsuario() == TipoUsuario.FERIANTE) {return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya es feriante");}
        // Opcional: Verificar si ya tiene una solicitud pendiente para no duplicar
        if (solicitudRepository.findByUsuario(usuario).isPresent() && !solicitudRepository.findByUsuario(usuario).get().isAprobada()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ya tienes una solicitud pendiente de revisión.");
        }

        // Creamos la entidad con los datos del DTO
        SolicitudParaFeriante solicitud = new SolicitudParaFeriante(
                usuario,
                dto.getNombreEmprendimiento(),
                dto.getDescripcion(),
                dto.getTelefono(),
                dto.getEmailEmprendimiento()
        );

        solicitudRepository.save(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body("Solicitud enviada correctamente.");
    }

    // ================================================================
    // 2️⃣ VER PENDIENTES (Administrador ve la lista)
    // ================================================================
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
    @Transactional // Importante: asegura que si falla algo, no se guarden datos a medias
    public ResponseEntity<?> aprobarSolicitud(@PathVariable int idSolicitud) {
        SolicitudParaFeriante solicitud = solicitudRepository.findById(idSolicitud).orElse(null);

        if (solicitud == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitud no encontrada");
        }
        if (solicitud.isAprobada()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Esta solicitud ya fue aprobada anteriormente");
        }

        // 1. Instanciamos el nuevo Feriante con los datos que estaban en "borrador" (la solicitud)
        Feriante nuevoFeriante = new Feriante(
                solicitud.getNombreEmprendimiento(),
                solicitud.getDescripcion(),
                solicitud.getTelefono(),
                solicitud.getEmailEmprendimiento(),
                EstadoUsuario.ACTIVO
        );

        // 2. Vinculamos el usuario al feriante (esto debería actualizar el rol a FERIANTE automáticamente si tu setter lo hace)
        nuevoFeriante.setUsuario(solicitud.getUsuario());

        // 3. Guardamos el nuevo feriante
        ferianteRepository.save(nuevoFeriante);

        // 4. Marcamos la solicitud como aprobada para que ya no salga en pendientes
        solicitud.setAprobada(true);
        solicitudRepository.save(solicitud);

        return ResponseEntity.ok("Solicitud aprobada: El usuario ahora es Feriante.");
    }
}