package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.ProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.CategoriaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.CategoriaProductoRepository;
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
    private final CategoriaProductoRepository categoriaRepository;

    public ProductoController(
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            FerianteRepository ferianteRepository,
            StandRepository standRepository,
            CloudinaryService cloudinaryService,
            CategoriaProductoRepository categoriaRepository
    ) {
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ferianteRepository = ferianteRepository;
        this.standRepository = standRepository;
        this.cloudinaryService = cloudinaryService;
        this.categoriaRepository = categoriaRepository;
    }

    // ========================================================
    // 🌍 VISTA PÚBLICA
    // ========================================================

    @GetMapping("/publicos")
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
            @RequestParam("precio") double precio,
            @RequestParam("categoriaId") int categoriaId,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication
    ) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());

        // Buscar la categoría seleccionada
        CategoriaProducto categoria = categoriaRepository.findById(categoriaId)
                .orElse(null);
        if (categoria == null) {
            return ResponseEntity.badRequest().body("La categoría seleccionada no existe.");
        }

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStand(stand);
        producto.setCategoria(categoria); // Asignación de categoría única

        if (imagen != null && !imagen.isEmpty()) {
            Map<String, String> result = cloudinaryService.subirImagen(imagen);
            producto.setImagenUrl(result.get("url"));
            producto.setImagenPublicId(result.get("public_id"));
        }

        productoRepository.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductoDTO(producto));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> editarProducto(
            @PathVariable int id,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("precio") double precio,
            @RequestParam("categoriaId") int categoriaId,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication
    ) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null || producto.isEliminado() || !producto.getStand().equals(stand)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Producto no encontrado");
        }

        // Buscar y actualizar categoría
        CategoriaProducto categoria = categoriaRepository.findById(categoriaId).orElse(null);
        if (categoria == null) {
            return ResponseEntity.badRequest().body("La categoría seleccionada no existe.");
        }

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);

        if (imagen != null && !imagen.isEmpty()) {
            Map<String, String> result = cloudinaryService.reemplazarImagen(imagen, producto.getImagenPublicId());
            producto.setImagenUrl(result.get("url"));
            producto.setImagenPublicId(result.get("public_id"));
        }

        productoRepository.save(producto);
        return ResponseEntity.ok("Producto actualizado con éxito");
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoProducto(@PathVariable int id, Authentication authentication) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null || producto.isEliminado() || !producto.getStand().equals(stand)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tenés permiso.");
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

        if (producto.getImagenPublicId() != null) {
            cloudinaryService.borrarImagen(producto.getImagenPublicId());
        }

        producto.setEliminado(true);
        producto.setActivo(false);
        productoRepository.save(producto);
        return ResponseEntity.ok("Producto eliminado.");
    }

    // ========================================================
    // 🛠️ MÉTODOS AUXILIARES
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