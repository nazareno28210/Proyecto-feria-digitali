package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Espacio;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoEspacio;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EspacioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.ParticipacionRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoPago;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/espacios")
public class EspacioController {

    @Autowired
    private EspacioRepository espacioRepository;

    @Autowired
    private EdicionFeriaRepository edicionFeriaRepository;

    @Autowired
    private ParticipacionRepository participacionRepository;

    // GET /api/espacios/edicion/{edicionId} — excluye los eliminados (soft delete)
    @GetMapping("/edicion/{edicionId}")
    public ResponseEntity<List<Map<String, Object>>> listarPorEdicion(@PathVariable Integer edicionId) {
        List<Map<String, Object>> activos = espacioRepository.findByEdicionId(edicionId).stream()
                .filter(e -> e.getEstado() != EstadoEspacio.ELIMINADO)
                .map(e -> Map.<String, Object>of(
                        "id", e.getId(),
                        "nombre", e.getNombre(),
                        "precio", e.getPrecio(),
                        "estado", e.getEstado().toString()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(activos);
    }

    // POST /api/espacios — crea un espacio individual o múltiples espacios por zona
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        Integer edicionId = (Integer) body.get("edicionId");
        if (edicionId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "edicionId es obligatorio"));
        }

        EdicionFeria edicion = edicionFeriaRepository.findById(edicionId).orElse(null);
        if (edicion == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Edición no encontrada"));
        }

        // 🟢 1. Creación de Lote Individual (cuando viene 'nombre' directo)
        if (body.containsKey("nombre") && !body.containsKey("desde")) {
            String nombre = body.get("nombre") != null ? body.get("nombre").toString().trim() : "";
            if (nombre.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre del lote es obligatorio"));
            }
            if (!body.containsKey("precio") || body.get("precio") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El precio es obligatorio"));
            }
            Double precio = Double.valueOf(body.get("precio").toString());
            if (precio < 0 || precio > 5000000) {
                return ResponseEntity.badRequest().body(Map.of("error", "El precio debe estar entre $0 y $5.000.000"));
            }

            long actuales = espacioRepository.findByEdicionId(edicionId).stream().filter(e -> e.getEstado() != EstadoEspacio.ELIMINADO).count();
            Integer cupoMaximo = (edicion.getFeria() != null && edicion.getFeria().getCapacidad() != null) ? edicion.getFeria().getCapacidad() : 0;
            if (cupoMaximo > 0 && (actuales + 1) > cupoMaximo) {
                return ResponseEntity.badRequest().body(Map.of("error", "No puedes crear más stands. Solo quedan " + (cupoMaximo - actuales) + " lugares disponibles en el cupo."));
            }

            if (espacioRepository.existsByEdicionIdAndNombreAndEstadoNot(edicionId, nombre, EstadoEspacio.ELIMINADO)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El espacio '" + nombre + "' ya existe en esta edición."));
            }

            Espacio nuevo = new Espacio(nombre, precio, EstadoEspacio.DISPONIBLE, edicion);
            espacioRepository.save(nuevo);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Espacio '" + nombre + "' creado correctamente"));
        }

        // 🟢 2. Creación Masiva por Zona
        String nombreZona = (String) body.get("nombreZona");
        Double precio = Double.valueOf(body.get("precio").toString());
        Integer desde = Integer.valueOf(body.get("desde").toString());
        Integer hasta = Integer.valueOf(body.get("hasta").toString());

        if (desde > hasta) {
            return ResponseEntity.badRequest().body(Map.of("error", "El valor 'desde' no puede ser mayor que 'hasta'"));
        }
        if (precio < 0 || precio > 5000000) {
            return ResponseEntity.badRequest().body(Map.of("error", "El precio debe estar entre $0 y $5.000.000"));
        }

        long actuales = espacioRepository.findByEdicionId(edicionId).stream().filter(e -> e.getEstado() != EstadoEspacio.ELIMINADO).count();
        int cantidadNueva = hasta - desde + 1;
        Integer cupoMaximo = (edicion.getFeria() != null && edicion.getFeria().getCapacidad() != null) ? edicion.getFeria().getCapacidad() : 0;
        if (cupoMaximo > 0 && (actuales + cantidadNueva) > cupoMaximo) {
            return ResponseEntity.badRequest().body(Map.of("error", "No puedes crear " + cantidadNueva + " stands. Solo quedan " + (cupoMaximo - actuales) + " lugares disponibles en el cupo."));
        }

        List<Espacio> nuevos = new ArrayList<>();
        for (int i = desde; i <= hasta; i++) {
            String nombre = nombreZona + " - Stand " + i;
            if (espacioRepository.existsByEdicionIdAndNombreAndEstadoNot(edicionId, nombre, EstadoEspacio.ELIMINADO)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El espacio '" + nombre + "' ya existe en esta edición."));
            }
            nuevos.add(new Espacio(nombre, precio, EstadoEspacio.DISPONIBLE, edicion));
        }
        espacioRepository.saveAll(nuevos);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Se crearon " + (hasta - desde + 1) + " espacio(s) correctamente"));
    }

    // PUT /api/espacios/{id} — actualiza nombre y/o precio
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEspacio(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Espacio espacio = espacioRepository.findById(id).orElse(null);
        if (espacio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Espacio no encontrado"));
        }
        if (espacio.getEstado() == EstadoEspacio.OCUPADO) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se puede modificar un stand que ya fue ocupado/pagado"));
        }
        boolean tieneInteresados = participacionRepository.findAll().stream()
                .anyMatch(p -> p.getEspacio() != null && p.getEspacio().getId().equals(id)
                        && (p.getEstado() == EstadoParticipacion.PENDIENTE || p.getEstadoPago() == EstadoPago.SENADO));
        if (tieneInteresados) {
            return ResponseEntity.badRequest().body(Map.of("error", "No puedes modificar este lote. Hay un feriante con solicitud pendiente o seña activa."));
        }
        if (espacio.getEdicion() != null && espacio.getEdicion().getFechaInicio() != null && espacio.getEdicion().getFechaInicio().isBefore(java.time.LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se pueden modificar lotes de una edición que ya ha comenzado."));
        }
        // Actualizar nombre si viene en el body
        if (body.containsKey("nombre") && body.get("nombre") != null) {
            String nuevoNombre = body.get("nombre").toString().trim();
            if (!nuevoNombre.isEmpty()) {
                espacio.setNombre(nuevoNombre);
            }
        }
        // Actualizar precio si viene en el body
        if (body.containsKey("precio") && body.get("precio") != null) {
            Double nuevoPrecio = Double.valueOf(body.get("precio").toString());
            if (nuevoPrecio < 0 || nuevoPrecio > 5000000) {
                return ResponseEntity.badRequest().body(Map.of("error", "El precio debe estar entre $0 y $5.000.000"));
            }
            espacio.setPrecio(nuevoPrecio);
        }
        return ResponseEntity.ok(espacioRepository.save(espacio));
    }

    // PUT /api/espacios/edicion/{edicionId}/actualizar-zona — actualización masiva por zona
    @PutMapping("/edicion/{edicionId}/actualizar-zona")
    public ResponseEntity<?> actualizarPrecioPorZona(@PathVariable Integer edicionId, @RequestBody Map<String, Object> body) {
        String nombreZona = body.get("nombreZona") != null ? body.get("nombreZona").toString().trim() : "";
        Object precioObj = body.get("nuevoPrecio") != null ? body.get("nuevoPrecio") : body.get("precio");
        if (nombreZona.isEmpty() || precioObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar nombreZona y nuevoPrecio"));
        }
        Double nuevoPrecio = Double.valueOf(precioObj.toString());
        if (nuevoPrecio < 0 || nuevoPrecio > 5000000) {
            return ResponseEntity.badRequest().body(Map.of("error", "El precio debe estar entre $0 y $5.000.000"));
        }

        List<Espacio> espaciosEdicion = espacioRepository.findByEdicionId(edicionId).stream()
                .filter(e -> e.getEstado() != EstadoEspacio.ELIMINADO)
                .filter(e -> e.getNombre() != null && e.getNombre().toLowerCase().startsWith((nombreZona + " -").toLowerCase()))
                .collect(Collectors.toList());

        if (espaciosEdicion.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No se encontraron espacios en la zona especificada"));
        }

        for (Espacio e : espaciosEdicion) {
            if (e.getEstado() == EstadoEspacio.OCUPADO) {
                return ResponseEntity.badRequest().body(Map.of("error", "La zona contiene stands ocupados (" + e.getNombre() + "). No se puede aplicar el cambio masivo."));
            }
            if (e.getEdicion() != null && e.getEdicion().getFechaInicio() != null && e.getEdicion().getFechaInicio().isBefore(java.time.LocalDate.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pueden modificar precios de una edición que ya ha comenzado."));
            }
            e.setPrecio(nuevoPrecio);
        }
        espacioRepository.saveAll(espaciosEdicion);

        return ResponseEntity.ok(Map.of("mensaje", "Se actualizaron " + espaciosEdicion.size() + " espacio(s) en la zona '" + nombreZona + "'"));
    }

    // PATCH /api/espacios/actualizar-precio-lote — actualización estricta por lista de IDs
    @PatchMapping("/actualizar-precio-lote")
    public ResponseEntity<?> actualizarPrecioLote(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("espaciosIds") || body.get("espaciosIds") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar la lista de espaciosIds"));
        }
        Object precioObj = body.get("nuevoPrecio") != null ? body.get("nuevoPrecio") : body.get("precio");
        if (precioObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar nuevoPrecio"));
        }

        List<?> rawIds = (List<?>) body.get("espaciosIds");
        if (rawIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La lista de espaciosIds no puede estar vacía"));
        }

        Double nuevoPrecio = Double.valueOf(precioObj.toString());
        if (nuevoPrecio < 0 || nuevoPrecio > 5000000) {
            return ResponseEntity.badRequest().body(Map.of("error", "El precio debe estar entre $0 y $5.000.000"));
        }

        List<Integer> ids = rawIds.stream()
                .map(id -> Integer.valueOf(id.toString()))
                .collect(Collectors.toList());

        List<Espacio> espacios = espacioRepository.findAllById(ids);

        for (Espacio e : espacios) {
            if (e.getEstado() == EstadoEspacio.OCUPADO) {
                return ResponseEntity.badRequest().body(Map.of("error", "El stand '" + e.getNombre() + "' está ocupado. No se puede modificar el precio."));
            }
            if (e.getEdicion() != null && e.getEdicion().getFechaInicio() != null && e.getEdicion().getFechaInicio().isBefore(java.time.LocalDate.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pueden modificar precios de una edición que ya ha comenzado."));
            }
            e.setPrecio(nuevoPrecio);
        }

        espacioRepository.saveAll(espacios);
        return ResponseEntity.ok(Map.of("mensaje", "Se actualizaron " + espacios.size() + " espacio(s) correctamente"));
    }


    // DELETE /api/espacios/{id} — soft delete: marca como ELIMINADO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        Espacio espacio = espacioRepository.findById(id).orElse(null);
        if (espacio == null) {
            return ResponseEntity.notFound().build();
        }
        if (espacio.getEstado() == EstadoEspacio.OCUPADO) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se puede eliminar un stand que ya está ocupado"));
        }
        espacio.setEstado(EstadoEspacio.ELIMINADO);
        espacioRepository.save(espacio);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/espacios/{id}/estado — forzar estado físico del espacio
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoFisico(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        Espacio espacio = espacioRepository.findById(id).orElse(null);
        if (espacio == null) return ResponseEntity.notFound().build();

        EstadoEspacio nuevoEstado = EstadoEspacio.valueOf(body.get("estado"));

        if (nuevoEstado == EstadoEspacio.DISPONIBLE) {
            boolean ferianteActivo = participacionRepository.findAll().stream()
                    .anyMatch(p -> p.getEspacio() != null && p.getEspacio().getId().equals(id)
                            && (p.getEstado() == EstadoParticipacion.CONFIRMADO || p.getEstado() == EstadoParticipacion.PENDIENTE));
            if (ferianteActivo) {
                return ResponseEntity.badRequest().body(Map.of("error", "No puedes forzar el lote a DISPONIBLE porque hay un feriante activo asignado. Cancela su postulación primero."));
            }
            espacio.setMotivoMantenimiento(null);
        } else if (nuevoEstado == EstadoEspacio.MANTENIMIENTO) {
            String motivo = body.get("motivo");
            if (motivo == null || motivo.isBlank()) {
                motivo = body.get("motivoMantenimiento");
            }
            espacio.setMotivoMantenimiento(motivo);
        }

        espacio.setEstado(nuevoEstado);
        return ResponseEntity.ok(espacioRepository.save(espacio));
    }
}
