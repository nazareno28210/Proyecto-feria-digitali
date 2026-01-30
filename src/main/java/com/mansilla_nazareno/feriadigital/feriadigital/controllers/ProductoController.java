package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.ProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioRepository;
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
@RequestMapping("/api")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FerianteRepository ferianteRepository;

    @Autowired
    private StandRepository standRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public ProductoController(ProductoRepository productoRepository){this.productoRepository=productoRepository;}

    @GetMapping("/productos")
    public List<ProductoDTO> getProductos(){
        return productoRepository.findAll()
                .stream()
                .map(producto -> new ProductoDTO(producto))
                .toList();
    }

    @GetMapping("/productos/{id}")
    public ProductoDTO getProductoDTO(@PathVariable Integer id){
        return productoRepository.findById(id)
                .map(ProductoDTO::new)
                .orElse(null);
    }

    // 🔹 Obtener todos los productos del stand del feriante logueado
    @GetMapping("/productos/mis-productos")
    public ResponseEntity<?> getMisProductos(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);

        if (feriante == null) {
            return new ResponseEntity<>("No se encontró feriante asociado al usuario", HttpStatus.NOT_FOUND);
        }

        Stand stand = standRepository.findByFeriante(feriante);
        if (stand == null) {
            return new ResponseEntity<>("El feriante no tiene un stand asignado", HttpStatus.NOT_FOUND);
        }

        List<ProductoDTO> productos = productoRepository.findByStand(stand)
                .stream()
                .map(ProductoDTO::new)
                .toList();

        return new ResponseEntity<>(productos, HttpStatus.OK);
    }


    // 🔹 Crear nuevo producto
    @PostMapping("/productos")
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);
        Stand stand = standRepository.findByFeriante(feriante);

        if (stand == null) {
            return new ResponseEntity<>("No se encontró stand para este feriante", HttpStatus.BAD_REQUEST);
        }

        producto.setStand(stand);
        productoRepository.save(producto);
        return new ResponseEntity<>("Producto creado correctamente", HttpStatus.CREATED);
    }
    // 🔹 Editar producto
    @PutMapping("/productos/{id}")
    public ResponseEntity<?> editarProducto(@PathVariable Integer id, @RequestBody Producto productoActualizado, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);
        Stand stand = standRepository.findByFeriante(feriante);
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null || !producto.getStand().equals(stand)) {
            return new ResponseEntity<>("Producto no encontrado o no pertenece a tu stand", HttpStatus.FORBIDDEN);
        }

        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setPrecio(productoActualizado.getPrecio());
        producto.setImagenUrl(productoActualizado.getImagenUrl());
        productoRepository.save(producto);

        return new ResponseEntity<>("Producto actualizado correctamente", HttpStatus.OK);
    }
    // 🔹 Eliminar producto
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Integer id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);
        Stand stand = standRepository.findByFeriante(feriante);
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null || !producto.getStand().equals(stand)) {
            return new ResponseEntity<>("Producto no encontrado o no pertenece a tu stand", HttpStatus.FORBIDDEN);
        }

        productoRepository.delete(producto);
        return new ResponseEntity<>("Producto eliminado correctamente", HttpStatus.OK);
    }


    @PostMapping("/productos/{id}/imagen")
    public ResponseEntity<?> cambiarImagenProducto(
            @PathVariable Integer id,
            @RequestParam("imagen") MultipartFile imagen,
            Authentication authentication
    ) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);

        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        // seguridad: el producto debe ser del stand del feriante
        if (!producto.getStand().getFeriante().equals(feriante)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, String> resultado =
                cloudinaryService.reemplazarImagen(
                        imagen,
                        producto.getImagenPublicId()
                );

        producto.setImagenUrl(resultado.get("url"));
        producto.setImagenPublicId(resultado.get("public_id"));

        productoRepository.save(producto);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Imagen del producto actualizada",
                "url", producto.getImagenUrl()
        ));
    }
    @DeleteMapping("/productos/{id}/imagen")
    public ResponseEntity<?> borrarImagenProducto(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName());
        Feriante feriante = ferianteRepository.findByUsuario(usuario);

        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        if (!producto.getStand().getFeriante().equals(feriante)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (producto.getImagenPublicId() != null) {
            cloudinaryService.borrarImagen(producto.getImagenPublicId());
        }

        producto.setImagenPublicId(null);
        producto.setImagenUrl(null); // vuelve al default

        productoRepository.save(producto);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Imagen del producto eliminada"
        ));
    }



}
