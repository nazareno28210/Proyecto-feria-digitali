package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.ProductoCrearDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.ProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.TipoVenta;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FerianteRepository ferianteRepository;
    private final StandRepository standRepository;
    private final CloudinaryService cloudinaryService;

    public ProductoController(
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            FerianteRepository ferianteRepository,
            StandRepository standRepository,
            CloudinaryService cloudinaryService
    ) {
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ferianteRepository = ferianteRepository;
        this.standRepository = standRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // ========================================================
    // 🌍 VISTA PÚBLICA
    // ========================================================

    @GetMapping("/productos/publicos")
    public ResponseEntity<List<ProductoDTO>> getProductosPublicos() {
        List<ProductoDTO> productos = productoRepository.findByActivoTrueAndEliminadoFalse()
                .stream()
                .map(ProductoDTO::new)
                .toList();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> getProducto(@PathVariable int id) {
        return productoRepository.findById(id)
                .filter(p -> p.isActivo() && !p.isEliminado())
                .map(producto -> ResponseEntity.ok(new ProductoDTO(producto)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ========================================================
    // 🧑‍🌾 GESTIÓN DEL FERIANTE (CRUD)
    // ========================================================

    @GetMapping("/mios")
    public ResponseEntity<List<ProductoDTO>> getMisProductos(Authentication authentication) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());
        List<ProductoDTO> productos = productoRepository.findByStandAndEliminadoFalse(stand)
                .stream()
                .map(ProductoDTO::new)
                .toList();
        return ResponseEntity.ok(productos);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> crearProducto(
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("tipoVenta") String tipoVentaStr, // "PESO", "LONGITUD", "UNIDAD"
            @RequestParam("unidadMedida") String unidad,    // "kg", "m", "un", etc.
            @RequestParam("precio") double precio,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication
    ) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());

        Producto producto = new Producto();

        // 1. Convertir y Validar el Tipo de Venta
        TipoVenta tipo;
        try {
            tipo = TipoVenta.valueOf(tipoVentaStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Tipo de venta no válido");
        }

        // 2. Delimitar la unidad de medida según el tipo
        producto.setTipoVenta(tipo);
        if (tipo == TipoVenta.UNIDAD) {
            producto.setUnidadMedida("un"); // Forzamos unidad fija para remeras, etc.
        } else {
            producto.setUnidadMedida(unidad.toLowerCase()); // kg, g, m, cm, mm
        }

        // 3. Setear campos básicos
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStand(stand);

        // 4. Lógica de Cloudinary (Imagen)
        if (imagen != null && !imagen.isEmpty()) {
            Map<String, String> result = cloudinaryService.subirImagen(imagen);
            producto.setImagenUrl(result.get("url"));
            producto.setImagenPublicId(result.get("public_id"));
        } else {
            producto.setImagenUrl(Producto.IMAGEN_DEFAULT);
        }

        // 5. Guardar y retornar
        productoRepository.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductoDTO(producto));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> editarProducto(
            @PathVariable int id,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("precio") double precio,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication
    ) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null || producto.isEliminado() || !producto.getStand().equals(stand)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Producto no encontrado");
        }

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);

        // Actualizar imagen si se envía una nueva
        if (imagen != null && !imagen.isEmpty()) {
            // Reemplazar en Cloudinary (borra la vieja y sube la nueva)
            Map<String, String> result = cloudinaryService.reemplazarImagen(imagen, producto.getImagenPublicId());
            producto.setImagenUrl(result.get("url"));
            producto.setImagenPublicId(result.get("public_id"));
        }

        productoRepository.save(producto);
        return ResponseEntity.ok("Producto actualizado con éxito");
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoProducto(
            @PathVariable int id,
            Authentication authentication
    ) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null || producto.isEliminado() || !producto.getStand().equals(stand)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para modificar este producto");
        }

        producto.setActivo(!producto.isActivo());
        productoRepository.save(producto);

        String status = producto.isActivo() ? "activado" : "desactivado";
        return ResponseEntity.ok("Producto " + status + " correctamente");
    }

    @PutMapping("/{id}/eliminar")
    public ResponseEntity<?> eliminarProductoLogico(@PathVariable int id, Authentication authentication) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null || !producto.getStand().equals(stand)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso.");
        }

        // Opcional: Borrar la imagen de Cloudinary al eliminar el producto de forma lógica
        if (producto.getImagenPublicId() != null) {
            cloudinaryService.borrarImagen(producto.getImagenPublicId());
        }

        producto.setEliminado(true);
        producto.setActivo(false);
        productoRepository.save(producto);
        return ResponseEntity.ok("Producto eliminado.");
    }

    // ========================================================
    // 🛠️ MÉTODOS AUXILIARES (Nivel de clase, no anidados)
    // ========================================================

    private Stand obtenerStandDelUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) throw new RuntimeException("Usuario no encontrado");

        Feriante feriante = ferianteRepository.findByUsuario(usuario);
        if (feriante == null) throw new RuntimeException("El usuario no es feriante");

        Stand stand = standRepository.findByFeriante(feriante);
        if (stand == null) throw new RuntimeException("El feriante no tiene stand");

        return stand;
    }
}