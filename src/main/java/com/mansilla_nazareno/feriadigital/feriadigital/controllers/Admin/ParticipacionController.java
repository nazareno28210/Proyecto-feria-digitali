package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin.ParticipacionDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.*;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EspacioRepository;
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

    @Autowired
    private EspacioRepository espacioRepository;

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
    public ResponseEntity<?> cambiarEstadoAsistencia(@PathVariable Integer id, @RequestParam EstadoParticipacion estado, @RequestParam(required = false) String motivo) {
        return participacionRepository.findById(id).map(participacion -> {

            // 🛡️ VALIDACIÓN DE CUPO AL ACEPTAR O PROMOVER DESDE LISTA DE ESPERA
            if (estado == EstadoParticipacion.PENDIENTE || estado == EstadoParticipacion.CONFIRMADO) {
                if (participacion.getEstado() == EstadoParticipacion.EN_ESPERA) {
                    EdicionFeria edicion = participacion.getEdicion();
                    Integer capacidad = edicion != null ? edicion.getCapacidad() : null;

                    if (edicion != null && capacidad != null) {
                        long ocupados = participacionRepository.findByEdicionId(edicion.getId()).stream()
                                .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO || p.getEstado() == EstadoParticipacion.PENDIENTE)
                                .count();

                        if (ocupados >= capacidad) {
                            return ResponseEntity.badRequest()
                                    .body(Map.of("error", "Error: Cupos todavía llenos. Falta liberar lugares en la lista de solicitudes."));
                        }
                    }
                }
            }

            if (estado == EstadoParticipacion.CONFIRMADO && participacion.getEstado() != EstadoParticipacion.PENDIENTE) {
                EdicionFeria edicion = participacion.getEdicion();

                long confirmados = participacionRepository.findByEdicionId(edicion.getId()).stream()
                        .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO)
                        .count();

                if (edicion != null && edicion.getCapacidad() != null && confirmados >= edicion.getCapacidad()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "No puedes aceptar más feriantes. La capacidad de " + edicion.getCapacidad() + " stands ya está completa."));
                }
            }
            // 🔒 Candado de Reembolsos
            if (estado == EstadoParticipacion.CANCELADO && participacion.getMontoAbonado() != null && participacion.getMontoAbonado() > 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bloqueo Contable: El feriante tiene un saldo a favor de $" + participacion.getMontoAbonado() + ". Debes registrar la devolución manual (dejando el monto en $0) editando su gestión antes de poder quitarlo de la feria."));
            }

            EstadoParticipacion estadoAnterior = participacion.getEstado();
            participacion.setEstado(estado);

            // Regla de Ocupación, Liberación y Rechazo
            if (estado == EstadoParticipacion.CONFIRMADO) {
                if (participacion.getEspacio() != null) {
                    Espacio espacio = participacion.getEspacio();
                    espacio.setEstado(EstadoEspacio.OCUPADO);
                    espacioRepository.save(espacio);
                }
            } else if (estado == EstadoParticipacion.RECHAZADO || estado == EstadoParticipacion.CANCELADO) {
                if (participacion.getEspacio() != null) {
                    Espacio espacio = participacion.getEspacio();
                    espacio.setEstado(EstadoEspacio.DISPONIBLE); // Liberamos la mesa para otro feriante
                    espacioRepository.save(espacio);
                    participacion.setEspacio(null); // Cortamos el vínculo físico en la base de datos
                }
                // Guardamos el motivo del rechazo/cancelación
                participacion.setMotivoRechazo(motivo);
            }

            participacionRepository.save(participacion);
            return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado correctamente"));

        }).orElse(ResponseEntity.notFound().build());
    }

    // 🟢 5. CAJA Y UBICACIÓN: Actualizar dinero y mesa validando unicidad en la EDICIÓN actual
    @PatchMapping("/{id}/pago")
    public ResponseEntity<?> actualizarPagoYUbicacion(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        return participacionRepository.findById(id).map(participacion -> {

            // VALIDACIÓN HISTÓRICA: Edición inactiva
            if (participacion.getEdicion() != null && participacion.getEdicion().getEstado() != null) {
                String estadoEdicion = participacion.getEdicion().getEstado().toString();
                if (!estadoEdicion.equals("ACTIVA")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "No puedes modificar pagos ni ubicaciones de una edición que ya finalizó o se encuentra inactiva."));
                }
            }

            Double monto = payload.containsKey("montoAbonado") ? Double.valueOf(payload.get("montoAbonado").toString()) : participacion.getMontoAbonado();

            if (monto < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "El monto abonado no puede ser negativo."));
            }

            boolean tieneEspacioPrevio = participacion.getEspacio() != null;
            boolean enviaEspacioNuevo = payload.containsKey("espacioId") && payload.get("espacioId") != null && !payload.get("espacioId").toString().isEmpty();

            if (monto > 0 && !tieneEspacioPrevio && !enviaEspacioNuevo) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debes asignar un lote/stand físico antes de poder registrar un pago."));
            }

            // 1. Asignación o actualización de Espacio
            Espacio espacioActual = participacion.getEspacio();
            if (payload.containsKey("espacioId") && payload.get("espacioId") != null) {
                Integer espacioId = Integer.valueOf(payload.get("espacioId").toString());
                if (espacioActual == null || !espacioActual.getId().equals(espacioId)) {
                    Espacio nuevoEspacio = espacioRepository.findById(espacioId).orElse(null);
                    if (nuevoEspacio != null && nuevoEspacio.getEstado() == EstadoEspacio.OCUPADO) {
                        return ResponseEntity.badRequest().body(Map.of("error", "El stand seleccionado acaba de ser ocupado."));
                    }
                    // CANDADO DE DEVOLUCIONES
                    if (nuevoEspacio != null) {
                        Double precioNuevoStand = nuevoEspacio.getPrecio();
                        Double dineroAbonado = participacion.getMontoAbonado() != null ? participacion.getMontoAbonado() : 0.0;
                        if (dineroAbonado > precioNuevoStand) {
                            return ResponseEntity.badRequest().body(Map.of("error", "El feriante ya abonó $" + dineroAbonado + ", pero el nuevo stand vale $" + precioNuevoStand + ". Debes regularizar el saldo a favor (devolución) antes de reasignarlo a un lote más barato."));
                        }
                    }
                    if (espacioActual != null) {
                        espacioActual.setEstado(EstadoEspacio.DISPONIBLE);
                        espacioRepository.save(espacioActual);
                    }
                    nuevoEspacio.setEstado(EstadoEspacio.OCUPADO);
                    espacioRepository.save(nuevoEspacio);
                    participacion.setEspacio(nuevoEspacio);
                    espacioActual = nuevoEspacio;
                }
            }

            // 2. Cálculo automático del Estado de Pago
            if (espacioActual != null) {
                Double precioStand = espacioActual.getPrecio();
                if (monto >= precioStand) {
                    participacion.setEstadoPago(EstadoPago.PAGADO);
                    participacion.setMontoAbonado(precioStand);
                } else if (monto > 0) {
                    participacion.setEstadoPago(EstadoPago.SENADO);
                    participacion.setMontoAbonado(monto);
                } else {
                    participacion.setEstadoPago(EstadoPago.DEBE);
                    participacion.setMontoAbonado(0.0);
                }
            } else {
                participacion.setMontoAbonado(monto);
                participacion.setEstadoPago(monto > 0 ? EstadoPago.SENADO : EstadoPago.DEBE);
            }

            // 3. Sincronización automática: Estado de Pago → Estado físico del Espacio
            if (participacion.getEspacio() != null) {
                Espacio espacioFisico = participacion.getEspacio();
                EstadoPago estadoPagoFinal = participacion.getEstadoPago();
                if (estadoPagoFinal == EstadoPago.DEBE) {
                    espacioFisico.setEstado(EstadoEspacio.RESERVADO);
                } else if (estadoPagoFinal == EstadoPago.SENADO || estadoPagoFinal == EstadoPago.PAGADO) {
                    espacioFisico.setEstado(EstadoEspacio.OCUPADO);
                }
                espacioRepository.save(espacioFisico);
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

        // 1. BUSCAR EDICIÓN Y STAND
        EdicionFeria edicion = edicionFeriaRepository.findById(edicionId).orElse(null);
        Stand stand = standRepository.findById(standId).orElse(null);

        if (edicion == null || stand == null) return ResponseEntity.notFound().build();

        // 3. CAPTURA Y VALIDACIÓN DEL LOTE ELEGIDO (Motor Financiero)
        Espacio espacioElegido = null;
        if (dto.getEspacioId() != null) {
            espacioElegido = espacioRepository.findById(dto.getEspacioId()).orElse(null);
            
            if (espacioElegido != null) {
                // Validar que el espacio pertenezca a la edición correcta
                if (!espacioElegido.getEdicion().getId().equals(edicionId)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "El lote seleccionado no pertenece a esta edición de la feria."));
                }
                // Validar que el espacio no haya sido tomado un segundo antes por otra persona
                if (espacioElegido.getEstado() == EstadoEspacio.OCUPADO) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Lo sentimos, el lote que elegiste acaba de ser ocupado. Por favor, selecciona otro."));
                }
            }
        }

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

        // 🛡️ ASIGNACIÓN DINÁMICA DE ESTADO SEGÚN CUPO
        long ocupados = participacionRepository.findByEdicionId(edicionId).stream()
                .filter(p -> p.getEstado() == EstadoParticipacion.CONFIRMADO || p.getEstado() == EstadoParticipacion.PENDIENTE)
                .count();

        EstadoParticipacion estadoInicial = (edicion != null && edicion.getCapacidad() != null && ocupados >= edicion.getCapacidad())
                ? EstadoParticipacion.EN_ESPERA
                : EstadoParticipacion.PENDIENTE;

        // 2. VALIDACIÓN DE DOBLE POSTULACIÓN EN LA MISMA EDICIÓN
        Optional<Participacion> existenteOpt = participacionRepository.findByEdicionIdAndStandId(edicionId, standId);
        if (existenteOpt.isPresent()) {
            Participacion existente = existenteOpt.get();
            if (existente.getEstado() != EstadoParticipacion.CANCELADO) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya enviaste una solicitud para esta edición de la feria"));
            }

            // Reseteo seguro si re-postula tras una cancelación previa
            existente.setEstado(estadoInicial);
            existente.setEstadoPago(EstadoPago.DEBE);
            existente.setMontoAbonado(0.0);
            existente.setEspacio(espacioElegido); // Genera la deuda basada en el precio del lote

            participacionRepository.save(existente);
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud enviada nuevamente con éxito."));
        }

        Participacion nueva = new Participacion();
        nueva.setEdicion(edicion); // Seteamos la edición correspondiente
        nueva.setStand(stand);
        nueva.setEstado(estadoInicial);
        nueva.setEspacio(espacioElegido); // Genera la deuda inicial basada en el precio

        participacionRepository.save(nueva);
        return ResponseEntity.ok(Map.of("mensaje", "Solicitud enviada"));
    }

    // 🟢 NUEVO: Listar TODAS las participaciones (para el Dashboard General)
    @GetMapping
    public ResponseEntity<List<ParticipacionDTO>> obtenerTodas() {
        List<ParticipacionDTO> todas = participacionRepository.findAll()
                .stream()
                .map(ParticipacionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(todas);
    }
}