package com.mansilla_nazareno.feriadigital.feriadigital.controllers.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante.ProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Feriante;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.FerianteRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        producto.setImagen(productoActualizado.getImagen());
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





}
