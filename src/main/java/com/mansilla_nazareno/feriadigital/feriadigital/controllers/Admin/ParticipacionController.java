package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.ParticipacionDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.*;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/participaciones")
public class ParticipacionController {

    @Autowired
    private ParticipacionRepository participacionRepository;

    @Autowired
    private FeriaRepository feriaRepository;

    @Autowired
    private StandRepository standRepository;

    // 🟢 1. PARA EL ADMIN: Trae TODOS (Pendientes, Confirmados, Cancelados)
    @GetMapping("/feria/{feriaId}")
    public ResponseEntity<List<ParticipacionDTO>> obtenerParticipantesPorFeria(@PathVariable Integer feriaId) {
        List<ParticipacionDTO> participantes = participacionRepository.findByFeriaId(feriaId)
                .stream()
                .map(ParticipacionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(participantes);
    }

    // 🟢 2. PARA EL PÚBLICO: Solo muestra si está CONFIRMADO y ya PAGÓ/SEÑÓ
    @GetMapping("/feria/{feriaId}/publico")
    public ResponseEntity<List<ParticipacionDTO>> obtenerParticipantesConfirmadosYPagos(@PathVariable Integer feriaId) {
        List<ParticipacionDTO> participantes = participacionRepository.findByFeriaId(feriaId)
                .stream()
                .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO && p.getEstadoPago() != EstadoPago.DEBE)
                .map(ParticipacionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(participantes);
    }

    // 🟢 3. PARA EL FERIANTE: Ver sus propias participaciones (Filtro del Modal)
    @GetMapping("/stand/{standId}")
    public ResponseEntity<List<ParticipacionDTO>> obtenerParticipacionesPorStand(@PathVariable Integer standId) {
        List<ParticipacionDTO> participaciones = participacionRepository.findByStandId(standId)
                .stream()
                .map(ParticipacionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(participaciones);
    }

    // 🟢 4. MODERACIÓN: Aceptar o Rechazar solicitud
    @PatchMapping("/{id}/estado-asistencia")
    public ResponseEntity<?> cambiarEstadoAsistencia(@PathVariable Integer id, @RequestParam EstadoParticipacion estado) {
        return participacionRepository.findById(id).map(participacion -> {

            // 🛡️ VALIDACIÓN DE CUPO AL ACEPTAR
            if (estado == EstadoParticipacion.CONFIRMADO) {
                Feria feria = participacion.getFeria();

                // Contamos cuántos feriantes ya están CONFIRMADOS en esta feria
                long confirmados = participacionRepository.findByFeriaId(feria.getId()).stream()
                        .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO)
                        .count();

                if (feria.getCapacidad() != null && confirmados >= feria.getCapacidad()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "No puedes aceptar más feriantes. La capacidad de " + feria.getCapacidad() + " stands ya está completa."));
                }
            }
            participacion.setEstado(estado);
            participacionRepository.save(participacion);
            return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado correctamente"));

        }).orElse(ResponseEntity.notFound().build());
    }

    // 🟢 5. CAJA Y UBICACIÓN: Actualizar dinero y número de mesa con validaciones
    @PatchMapping("/{id}/pago")
    public ResponseEntity<?> actualizarPagoYUbicacion(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        return participacionRepository.findById(id).map(participacion -> {

            // 1. Extraemos los valores del payload para comparar
            Double monto = payload.containsKey("montoAbonado") ? Double.valueOf(payload.get("montoAbonado").toString()) : participacion.getMontoAbonado();
            EstadoPago estado = payload.containsKey("estadoPago") ? EstadoPago.valueOf(payload.get("estadoPago").toString()) : participacion.getEstadoPago();

            // 🛡️ NUEVA: VALIDACIÓN DE CONSISTENCIA DE PAGO
            // Si el monto es mayor a 0, el estado NO puede ser DEBE
            if (monto > 0 && estado == EstadoPago.DEBE) {
                return ResponseEntity.badRequest().body(Map.of("error",
                        "No se puede registrar un monto si el estado es 'DEBE'. Seleccione 'SEÑADO' o 'PAGADO'."));
            }

            // Si el monto es 0 y el estado es diferente a DEBE, también es un error
            if (monto == 0 && estado != EstadoPago.DEBE) {
                return ResponseEntity.badRequest().body(Map.of("error",
                        "Para estados 'SEÑADO' o 'PAGADO', el monto debe ser mayor a 0."));
            }

            // Validación de Monto Negativo
            if (monto < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "El monto abonado no puede ser negativo."));
            }
            participacion.setMontoAbonado(monto);
            participacion.setEstadoPago(estado);

            // Validación de Ubicación (Unicidad y Rango)
            if (payload.containsKey("numeroStand")) {
                Object numObj = payload.get("numeroStand");
                if (numObj != null && !numObj.toString().isEmpty()) {
                    Integer mesaIngresada = Integer.valueOf(numObj.toString());
                    Integer capacidadFeria = participacion.getFeria().getCapacidad();

                    if (capacidadFeria != null && (mesaIngresada < 1 || mesaIngresada > capacidadFeria)) {
                        return ResponseEntity.badRequest().body(Map.of("error",
                                "Número de mesa fuera de rango (1-" + capacidadFeria + ")."));
                    }

                    boolean ocupada = participacionRepository.existsByFeriaIdAndNumeroStandAndIdNot(
                            participacion.getFeria().getId(), mesaIngresada, id
                    );

                    if (ocupada) {
                        return ResponseEntity.badRequest().body(Map.of("error", "La mesa " + mesaIngresada + " ya está asignada."));
                    }
                    participacion.setNumeroStand(mesaIngresada);
                } else {
                    participacion.setNumeroStand(null);
                }
            }
            participacionRepository.save(participacion);
            return ResponseEntity.ok(Map.of("mensaje", "Datos actualizados correctamente"));
        }).orElse(new ResponseEntity<>(Map.of("error", "Participación no encontrada"), HttpStatus.NOT_FOUND));
    }

    // 🟢 6. INSCRIPCIÓN: Con validación de cupo y existencia
    @PostMapping("/inscribir")
    public ResponseEntity<?> inscribirFeriante(@RequestBody Map<String, Integer> request) {
        Integer feriaId = request.get("feriaId");
        Integer standId = request.get("standId");
        LocalDate hoy = LocalDate.now();

        // 1. VALIDACIÓN DE DOBLE POSTULACIÓN
        Optional<Participacion> existenteOpt = participacionRepository.findByFeriaIdAndStandId(feriaId, standId);
        if (existenteOpt.isPresent()) {
            Participacion existente = existenteOpt.get();
            if (existente.getEstado() != EstadoParticipacion.CANCELADO) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya enviaste una solicitud para esta feria"));
            }
            // Lógica de reseteo (se mantiene igual)...
            existente.setEstado(EstadoParticipacion.PENDIENTE);
            existente.setEstadoPago(EstadoPago.DEBE);
            existente.setMontoAbonado(0.0);
            existente.setNumeroStand(null);
            participacionRepository.save(existente);
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud enviada nuevamente con éxito."));
        }

        // 2. BUSCAR FERIA Y STAND
        Feria feria = feriaRepository.findById(feriaId).orElse(null);
        Stand stand = standRepository.findById(standId).orElse(null);

        if (feria == null || stand == null) return ResponseEntity.notFound().build();

        // 🛡️ VALIDACIÓN DE VIGENCIA TEMPORAL (Ya implementada)
        if (feria.getFechaInicio() != null && feria.getFechaInicio().isBefore(hoy)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No puedes inscribirte a una feria que ya ha comenzado."));
        }

        // 🛡️ NUEVA: VALIDACIÓN DE PERFIL COMPLETO
        // Verificamos que el emprendimiento tenga descripción y foto cargada
        if (stand.getDescripcion() == null || stand.getDescripcion().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Debes completar la descripción de tu emprendimiento antes de postularte."));
        }

        // Suponiendo que el campo en tu entidad Stand se llama imagenUrl o fotoUrl
        if (stand.getImagenUrl() == null || stand.getImagenUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Debes subir un logo o imagen representativa de tu stand."));
        }

        // 3. VALIDACIÓN DE CUPO Y CREACIÓN
        long activos = participacionRepository.findByFeriaId(feriaId).stream()
                .filter(p -> p.getEstado() != EstadoParticipacion.CANCELADO)
                .count();

        if (feria.getCapacidad() != null && activos >= feria.getCapacidad()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La feria ha alcanzado su capacidad máxima."));
        }

        Participacion nueva = new Participacion(feria, stand, null, EstadoParticipacion.PENDIENTE);
        participacionRepository.save(nueva);

        return ResponseEntity.ok(Map.of("mensaje", "Solicitud enviada con éxito."));
    }
}