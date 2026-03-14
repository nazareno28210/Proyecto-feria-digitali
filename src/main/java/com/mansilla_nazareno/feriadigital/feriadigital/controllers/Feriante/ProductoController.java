package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.ProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.*;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.CategoriaProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.ResenaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private ResenaRepository resenaRepository;


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
    public ResponseEntity<ProductoDTO> getProductoDetalle(@PathVariable int id) {
        return productoRepository.findById(id)
                .filter(p -> !p.isEliminado())
                .map(producto -> {
                    // 1. Calculamos el promedio y cantidad de la DB
                    Double promedio = resenaRepository.getPromedioPorProducto(id);
                    Long cantidad = resenaRepository.getCantidadResenasPorProducto(id);

                    // 2. Creamos el DTO
                    ProductoDTO dto = new ProductoDTO(producto);

                    // 3. Cargamos los datos calculados
                    // Si 'promedio' es null (porque no hay reseñas), ponemos 0.0
                    dto.setPromedioEstrellas(promedio != null ? promedio : 0.0);
                    dto.setCantidadResenas(cantidad != null ? cantidad.intValue() : 0);

                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }


// ========================================================
// 🔎 BUSCADOR GLOBAL (VENTANA ÚNICA)
// ========================================================

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDTO>> buscarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer feriaId,
            @RequestParam(required = false) Double minPrecio,
            @RequestParam(required = false) Double maxPrecio,
            @RequestParam(defaultValue = "true") boolean soloActivos,
            @RequestParam(defaultValue = "true") boolean soloFeriasActivas
    ) {
        // Usamos el método avanzado del repositorio que filtra por todo esto
        List<ProductoDTO> resultados = productoRepository.buscarConFiltrosPro(
                        nombre, categoriaId, feriaId, minPrecio, maxPrecio, soloActivos, soloFeriasActivas)
                .stream().map(ProductoDTO::new).toList();
        return ResponseEntity.ok(resultados);
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
            @RequestParam("tipoVenta") String tipoVentaStr,
            @RequestParam("unidadMedida") String unidad,
            @RequestParam("precio") double precio,
            @RequestParam("categoriaId") int categoriaId,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen, // Foto principal
            @RequestParam(value = "imagenesExtras", required = false) List<MultipartFile> imagenesExtras, // <--- NUEVO
            Authentication authentication
    ) {
        // 1. Validaciones y obtención de dependencias [cite: 408-414]
        Stand stand = obtenerStandDelUsuario(authentication.getName());
        CategoriaProducto categoria = categoriaRepository.findById(categoriaId).orElse(null);

        if (categoria == null) {
            return ResponseEntity.badRequest().body("La categoría seleccionada no existe.");
        }

        TipoVenta tipo;
        try {
            tipo = TipoVenta.valueOf(tipoVentaStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Tipo de venta no válido");
        }

        // 2. Setear datos básicos [cite: 415-418]
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        producto.setTipoVenta(tipo);
        producto.setStand(stand);

        if (tipo == TipoVenta.UNIDAD) {
            producto.setUnidadMedida("un");
        } else {
            producto.setUnidadMedida(unidad != null ? unidad.toLowerCase() : "");
        }

        // 3. Imagen Principal (Portada) [cite: 419-420]
        if (imagen != null && !imagen.isEmpty()) {
            Map<String, String> result = cloudinaryService.subirImagen(imagen);
            producto.setImagenUrl(result.get("url"));
            producto.setImagenPublicId(result.get("public_id"));
        } else {
            producto.setImagenUrl(Producto.IMAGEN_DEFAULT);
        }

        // 4. 🟢 NUEVO: Procesar Galería de Imágenes Extras
        if (imagenesExtras != null && !imagenesExtras.isEmpty()) {
            for (MultipartFile archivo : imagenesExtras) {
                if (archivo != null && !archivo.isEmpty()) {
                    try {
                        Map<String, String> resultExtra = cloudinaryService.subirImagen(archivo);
                        // Creamos la entidad ImagenProducto y la vinculamos al producto
                        ImagenProducto nuevaImagen = new ImagenProducto(
                                resultExtra.get("url"),
                                resultExtra.get("public_id"),
                                producto
                        );
                        producto.getImagenes().add(nuevaImagen);
                    } catch (Exception e) {
                        // Opcional: Loguear error si una imagen falla pero continuar con las demás
                        System.err.println("Error subiendo imagen extra: " + e.getMessage());
                    }
                }
            }
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
            @RequestParam("tipoVenta") String tipoVentaStr,
            @RequestParam(value = "unidadMedida", required = false) String unidad,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen, // Nueva portada
            @RequestParam(value = "imagenesExtras", required = false) List<MultipartFile> imagenesExtras, // Nuevas para galería
            @RequestParam(value = "eliminarImagenIds", required = false) List<Long> eliminarIds, // IDs de galería a borrar
            @RequestParam(value = "borrarPortada", required = false, defaultValue = "false") boolean borrarPortada, // 🟢 NUEVO
            Authentication authentication
    ) {
        Stand stand = obtenerStandDelUsuario(authentication.getName());
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null || !producto.getStand().equals(stand)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No encontrado");
        }

        // 1. Validaciones básicas de negocio [cite: 95-98]
        CategoriaProducto categoria = categoriaRepository.findById(categoriaId).orElse(null);
        TipoVenta tipo = TipoVenta.valueOf(tipoVentaStr.toUpperCase());

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        producto.setTipoVenta(tipo);

        if (tipo == TipoVenta.UNIDAD) {
            producto.setUnidadMedida("un");
        } else {
            producto.setUnidadMedida(unidad != null ? unidad.toLowerCase() : "");
        }

        // 2. 🟢 NUEVO: Lógica de eliminación de la Portada Actual
        // Si el usuario marcó la "X" en la portada, la borramos de Cloudinary
        if (borrarPortada && producto.getImagenPublicId() != null) {
            cloudinaryService.borrarImagen(producto.getImagenPublicId());
            producto.setImagenUrl(Producto.IMAGEN_DEFAULT); // Reset a imagen por defecto [cite: 4]
            producto.setImagenPublicId(null);
        }

        // 3. Procesar Nueva Imagen de Portada (si se subió una) [cite: 99-102]
        if (imagen != null && !imagen.isEmpty()) {
            Map<String, String> result;
            // Si hay una portada vieja que NO borramos antes, la reemplazamos
            if (producto.getImagenPublicId() != null) {
                result = cloudinaryService.reemplazarImagen(imagen, producto.getImagenPublicId());
            } else {
                // Si estaba vacía o la borramos recién, subimos una nueva
                result = cloudinaryService.subirImagen(imagen);
            }
            producto.setImagenUrl(result.get("url"));
            producto.setImagenPublicId(result.get("public_id"));
        }

        // 4. Borrado de imágenes seleccionadas de la galería
        if (eliminarIds != null && !eliminarIds.isEmpty()) {
            producto.getImagenes().removeIf(img -> {
                if (eliminarIds.contains(img.getId())) {
                    cloudinaryService.borrarImagen(img.getPublicId());
                    return true;
                }
                return false;
            });
        }

        // 5. Agregar nuevas imágenes a la galería (Validación de límite)
        if (imagenesExtras != null && !imagenesExtras.isEmpty()) {
            // Validación de seguridad en el backend (Máximo 5 fotos extras en total)
            int espacioDisponible = 5 - producto.getImagenes().size();

            for (int i = 0; i < Math.min(imagenesExtras.size(), espacioDisponible); i++) {
                MultipartFile archivo = imagenesExtras.get(i);
                if (archivo != null && !archivo.isEmpty()) {
                    Map<String, String> res = cloudinaryService.subirImagen(archivo);
                    ImagenProducto nuevaImg = new ImagenProducto(res.get("url"), res.get("public_id"), producto);
                    producto.getImagenes().add(nuevaImg);
                }
            }
        }

        productoRepository.save(producto);
        return ResponseEntity.ok("Producto actualizado correctamente");
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