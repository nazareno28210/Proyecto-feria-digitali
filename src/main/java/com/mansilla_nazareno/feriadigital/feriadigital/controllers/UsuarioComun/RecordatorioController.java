package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoEdicion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.RecordatorioEdicion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.RecordatorioEdicionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/recordatorios")
public class RecordatorioController {

    @Autowired
    private RecordatorioEdicionRepository recordatorioEdicionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EdicionFeriaRepository edicionFeriaRepository;

    // 🔔 Consultar estado del recordatorio para el usuario logueado
    @GetMapping("/edicion/{edicionId}/estado")
    public ResponseEntity<?> obtenerEstadoRecordatorio(@PathVariable Integer edicionId, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("autenticado", false);
            response.put("activo", false);
            return ResponseEntity.ok(response);
        }

        Usuario usuarioLogueado = usuarioRepository.findByEmail(authentication.getName());
        if (usuarioLogueado == null) {
            response.put("autenticado", false);
            response.put("activo", false);
            return ResponseEntity.ok(response);
        }

        Optional<EdicionFeria> edicionOpt = edicionFeriaRepository.findById(edicionId);
        if (edicionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Edición de feria no encontrada");
        }

        boolean existe = recordatorioEdicionRepository.existsByUsuarioAndEdicionAndActivoTrue(usuarioLogueado, edicionOpt.get());
        response.put("autenticado", true);
        response.put("activo", existe);
        return ResponseEntity.ok(response);
    }

    // 🔔 Alternar/Activar recordatorio para una edición (Con borrado lógico)
    @PostMapping("/edicion/{edicionId}")
    @Transactional
    public ResponseEntity<?> alternarRecordatorio(@PathVariable Integer edicionId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión para activar el recordatorio.");
        }

        Usuario usuarioLogueado = usuarioRepository.findByEmail(authentication.getName());
        if (usuarioLogueado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado.");
        }

        Optional<EdicionFeria> edicionOpt = edicionFeriaRepository.findById(edicionId);
        if (edicionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Edición de feria no encontrada.");
        }

        EdicionFeria edicion = edicionOpt.get();
        if (edicion.getEstado() != EstadoEdicion.PROXIMA) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Solo se pueden activar recordatorios para ferias en estado PRÓXIMA.");
        }

        Optional<RecordatorioEdicion> existenteOpt = recordatorioEdicionRepository.findByUsuarioAndEdicion(usuarioLogueado, edicion);
        Map<String, Object> response = new HashMap<>();

        if (existenteOpt.isPresent()) {
            RecordatorioEdicion recordatorio = existenteOpt.get();
            if (recordatorio.isActivo()) {
                // 🛑 BORRADO LÓGICO: se desactiva sin eliminar de la base de datos
                recordatorio.setActivo(false);
                recordatorioEdicionRepository.save(recordatorio);
                response.put("activo", false);
                response.put("mensaje", "El recordatorio para esta feria ha sido cancelado.");
            } else {
                // 🔄 REACTIVACIÓN: se vuelve a marcar activo conservando el historial
                recordatorio.setActivo(true);
                recordatorio.setFechaSuscripcion(java.time.LocalDateTime.now());
                recordatorioEdicionRepository.save(recordatorio);
                response.put("activo", true);
                response.put("mensaje", "Esta feria será notificada por correo antes de su apertura.");
            }
        } else {
            RecordatorioEdicion nuevoRecordatorio = new RecordatorioEdicion(usuarioLogueado, edicion);
            recordatorioEdicionRepository.save(nuevoRecordatorio);
            response.put("activo", true);
            response.put("mensaje", "Esta feria será notificada por correo antes de su apertura.");
        }


        return ResponseEntity.ok(response);
    }

    @Autowired
    private com.mansilla_nazareno.feriadigital.feriadigital.services.Admin.EdicionScheduler edicionScheduler;

    // 🚀 Endpoint manual para pruebas y demostraciones (Ejecuta la tarea del scheduler a demanda)
    @PostMapping("/ejecutar-scheduler")
    public ResponseEntity<?> ejecutarSchedulerManualmente() {
        edicionScheduler.actualizarEstadosEdiciones();
        return ResponseEntity.ok("⚡ Tarea del Scheduler ejecutada manualmente con éxito. Revisa la consola o bandeja de correo.");
    }
}


