package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.models.*;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudParaFerianteController {

    @Autowired
    private SolicitudParaFerianteRepository solicitudRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    // ✅ Crear solicitud
    @PostMapping("/crear/{idUsuario}")
    public ResponseEntity<?> crearSolicitud(@PathVariable int idUsuario) {
        Usuario usuario = usuarioRepo.findById(idUsuario).orElse(null);

        if (usuario == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");

        if (usuario.getTipoUsuario() == TipoUsuario.FERIANTE)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya es feriante");

        if (solicitudRepo.findByUsuario(usuario).isPresent())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ya existe una solicitud pendiente");

        SolicitudParaFeriante solicitud = new SolicitudParaFeriante(usuario);
        solicitudRepo.save(solicitud);

        return ResponseEntity.status(HttpStatus.CREATED).body("Solicitud creada correctamente");
    }

    // ✅ Ver solicitudes pendientes (solo admin)
    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudParaFeriante>> obtenerPendientes() {
        List<SolicitudParaFeriante> pendientes = solicitudRepo.findAll()
                .stream().filter(s -> !s.isAprobada()).toList();
        return ResponseEntity.ok(pendientes);
    }

    // ✅ Aprobar solicitud
    @PostMapping("/aprobar/{idSolicitud}")
    public ResponseEntity<?> aprobarSolicitud(@PathVariable int idSolicitud) {
        SolicitudParaFeriante solicitud = solicitudRepo.findById(idSolicitud).orElse(null);

        if (solicitud == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitud no encontrada");

        solicitud.setAprobada(true);

        Usuario usuario = solicitud.getUsuario();
        usuario.setTipoUsuario(TipoUsuario.FERIANTE);

        usuarioRepo.save(usuario);
        solicitudRepo.save(solicitud);

        return ResponseEntity.ok("Solicitud aprobada: el usuario ahora es feriante");
    }
}
