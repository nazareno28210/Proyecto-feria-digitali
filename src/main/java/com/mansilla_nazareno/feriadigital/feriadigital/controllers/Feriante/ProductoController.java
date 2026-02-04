package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.ProductoCrearDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.ProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FerianteRepository ferianteRepository;
    private final StandRepository standRepository;

    public ProductoController(
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            FerianteRepository ferianteRepository,
            StandRepository standRepository
    ) {
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ferianteRepository = ferianteRepository;
        this.standRepository = standRepository;
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

    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(
            @RequestBody ProductoCrearDTO dto,
            Authentication authentication
    ) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setImagenUrl(dto.getImagen());
        producto.setActivo(true);
        producto.setEliminado(false);
        producto.setStand(stand);

        productoRepository.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductoDTO(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarProducto(
            @PathVariable int id,
            @RequestBody ProductoCrearDTO dto,
            Authentication authentication
    ) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null || producto.isEliminado() || !producto.getStand().equals(stand)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Producto no encontrado o no pertenece a tu stand");
        }

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setImagenUrl(dto.getImagen());

        productoRepository.save(producto);
        return ResponseEntity.ok("Producto actualizado correctamente");
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

        producto.setEliminado(true);
        producto.setActivo(false);
        productoRepository.save(producto);
        return ResponseEntity.ok("Producto eliminado de tu lista.");
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