package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.ParticipacionDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.*;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository; // 🟢 Cambiado por el de Ediciones
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
    private EdicionFeriaRepository edicionFeriaRepository; // 🟢 Inyectamos el nuevo repositorio

    @Autowired
    private StandRepository standRepository;

    // 🟢 1. PARA EL ADMIN: Trae TODOS los inscriptos de una EDICIÓN específica
    @GetMapping("/edicion/{edicionId}")
    public ResponseEntity<List<ParticipacionDTO>> obtenerParticipantesPorEdicion(@PathVariable Integer edicionId) {
        List<ParticipacionDTO> participantes = participacionRepository.findByEdicionId(edicionId)
                .stream()
                .map(ParticipacionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(participantes);
    }

    // 🟢 2. PARA EL PÚBLICO: Muestra stands CONFIRMADOS y PAGOS de una EDICIÓN
    @GetMapping("/edicion/{edicionId}/publico")
    public ResponseEntity<List<ParticipacionDTO>> obtenerParticipantesConfirmadosYPagos(@PathVariable Integer edicionId) {
        List<ParticipacionDTO> participantes = participacionRepository.findByEdicionId(edicionId)
                .stream()
                .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO && p.getEstadoPago() != EstadoPago.DEBE)
                .map(ParticipacionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(participantes);
    }

    // 🟢 3. PARA EL FERIANTE: Ver su propio historial de postulaciones (No cambia, usa StandId)
    @GetMapping("/stand/{standId}")
    public ResponseEntity<List<ParticipacionDTO>> obtenerParticipacionesPorStand(@PathVariable Integer standId) {
        List<ParticipacionDTO> participaciones = participacionRepository.findByStandId(standId)
                .stream()
                .map(ParticipacionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(participaciones);
    }

    // 🟢 4. MODERACIÓN: Aceptar o Rechazar solicitud validando el cupo de la feria base
    @PatchMapping("/{id}/estado-asistencia")
    public ResponseEntity<?> cambiarEstadoAsistencia(@PathVariable Integer id, @RequestParam EstadoParticipacion estado) {
        return participacionRepository.findById(id).map(participacion -> {

            // 🛡️ VALIDACIÓN DE CUPO AL ACEPTAR
            if (estado == EstadoParticipacion.CONFIRMADO) {
                EdicionFeria edicion = participacion.getEdicion();
                Feria feriaBase = edicion.getFeria(); // Bajamos a la plantilla para ver la capacidad

                // Contamos cuántos ya están confirmados exclusivamente en esta edición cronológica
                long confirmados = participacionRepository.findByEdicionId(edicion.getId()).stream()
                        .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO)
                        .count();

                if (feriaBase != null && feriaBase.getCapacidad() != null && confirmados >= feriaBase.getCapacidad()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "No puedes aceptar más feriantes. La capacidad de " + feriaBase.getCapacidad() + " stands ya está completa."));
                }
            }
            participacion.setEstado(estado);
            participacionRepository.save(participacion);
            return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado correctamente"));

        }).orElse(ResponseEntity.notFound().build());
    }

    // 🟢 5. CAJA Y UBICACIÓN: Actualizar dinero y mesa validando unicidad en la EDICIÓN actual
    @PatchMapping("/{id}/pago")
    public ResponseEntity<?> actualizarPagoYUbicacion(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        return participacionRepository.findById(id).map(participacion -> {

            Double monto = payload.containsKey("montoAbonado") ? Double.valueOf(payload.get("montoAbonado").toString()) : participacion.getMontoAbonado();
            EstadoPago estado = payload.containsKey("estadoPago") ? EstadoPago.valueOf(payload.get("estadoPago").toString()) : participacion.getEstadoPago();

            // Lógica semafórica de control de consistencia de dinero
            if (monto > 0 && estado == EstadoPago.DEBE) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se puede registrar un monto si el estado es 'DEBE'. Seleccione 'SEÑADO' o 'PAGADO'."));
            }
            if (monto == 0 && estado != EstadoPago.DEBE) {
                return ResponseEntity.badRequest().body(Map.of("error", "Para estados 'SEÑADO' o 'PAGADO', el monto debe ser mayor a 0."));
            }
            if (monto < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "El monto abonado no puede ser negativo."));
            }

            participacion.setMontoAbonado(monto);
            participacion.setEstadoPago(estado);

            // Validación de Ubicación Física (Mesa única por edición)
            if (payload.containsKey("numeroStand")) {
                Object numObj = payload.get("numeroStand");
                if (numObj != null && !numObj.toString().isEmpty()) {
                    Integer mesaIngresada = Integer.valueOf(numObj.toString());
                    Integer capacidadFeria = participacion.getEdicion().getFeria().getCapacidad();

                    if (capacidadFeria != null && (mesaIngresada < 1 || mesaIngresada > capacidadFeria)) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Número de mesa fuera de rango (1-" + capacidadFeria + ")."));
                    }

                    // Controlamos que nadie ocupe la misma mesa en el mismo fin de semana de este evento
                    boolean ocupada = participacionRepository.existsByEdicionIdAndNumeroStandAndIdNot(
                            participacion.getEdicion().getId(), mesaIngresada, id
                    );

                    if (ocupada) {
                        return ResponseEntity.badRequest().body(Map.of("error", "La mesa " + mesaIngresada + " ya está asignada para este evento."));
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

    // 🟢 6. INSCRIPCIÓN: Postulación apuntando al ID de la Edición Abierta
    @PostMapping("/inscribir")
    public ResponseEntity<?> inscribirFeriante(@RequestBody ParticipacionDTO dto) {
        Integer edicionId = dto.getEdicionId(); // Usamos el ID de edición mapeado en el DTO
        Integer standId = dto.getStandId();
        LocalDate hoy = LocalDate.now();

        // 1. VALIDACIÓN DE DOBLE POSTULACIÓN EN LA MISMA EDICIÓN
        Optional<Participacion> existenteOpt = participacionRepository.findByEdicionIdAndStandId(edicionId, standId);
        if (existenteOpt.isPresent()) {
            Participacion existente = existenteOpt.get();
            if (existente.getEstado() != EstadoParticipacion.CANCELADO) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya enviaste una solicitud para esta edición de la feria"));
            }

            // Reseteo seguro si re-postula tras una cancelación previa
            existente.setEstado(EstadoParticipacion.PENDIENTE);
            existente.setEstadoPago(EstadoPago.DEBE);
            existente.setMontoAbonado(0.0);
            existente.setNumeroStand(null);
            existente.setNumeroStandPreferido(dto.getNumeroStandPreferido());

            participacionRepository.save(existente);
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud enviada nuevamente con éxito."));
        }

        // 2. BUSCAR EDICIÓN Y STAND
        EdicionFeria edicion = edicionFeriaRepository.findById(edicionId).orElse(null);
        Stand stand = standRepository.findById(standId).orElse(null);

        if (edicion == null || stand == null) return ResponseEntity.notFound().build();

        // 🛡️ VALIDACIÓN DE VIGENCIA TEMPORAL (Evaluada sobre el inicio de la edición cronológica)
        if (edicion.getFechaInicio() != null && edicion.getFechaInicio().isBefore(hoy)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No puedes inscribirte a una edición de feria que ya ha comenzado."));
        }

        // 🛡️ VALIDACIÓN DE PERFIL COMPLETO DEL STAND
        if (stand.getDescripcion() == null || stand.getDescripcion().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Debes completar la descripción de tu emprendimiento antes de postularte."));
        }
        if (stand.getImagenUrl() == null || stand.getImagenUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Debes subir un logo o imagen representativa de tu stand."));
        }

        // 3. VALIDACIÓN DE CUPO SOBRE LA EDICIÓN ACTUAL
        long activos = participacionRepository.findByEdicionId(edicionId).stream()
                .filter(p -> p.getEstado() != EstadoParticipacion.CANCELADO)
                .count();

        Feria feriaBase = edicion.getFeria();
        if (feriaBase != null && feriaBase.getCapacidad() != null && activos >= feriaBase.getCapacidad()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La feria ha alcanzado su capacidad máxima para esta edición."));
        }

        Participacion nueva = new Participacion();
        nueva.setEdicion(edicion); // Seteamos la edición correspondiente
        nueva.setStand(stand);
        nueva.setEstado(EstadoParticipacion.PENDIENTE);
        nueva.setNumeroStandPreferido(dto.getNumeroStandPreferido());

        participacionRepository.save(nueva);
        return ResponseEntity.ok(Map.of("mensaje", "Solicitud enviada con preferencia"));
    }
}