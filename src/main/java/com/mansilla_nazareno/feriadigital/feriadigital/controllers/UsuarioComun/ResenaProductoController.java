package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun.ResenaProductoDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.ResenaProducto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.ResenaProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/resenas-producto")
public class ResenaProductoController {

    @Autowired
    private ResenaProductoRepository resenaProductoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public static class ResenaProductoRequest {
        @JsonProperty("producto_id")
        private Integer productoId;

        private Integer puntaje;

        private String comentario;

        public Integer getProductoId() {
            return productoId;
        }

        public void setProductoId(Integer productoId) {
            this.productoId = productoId;
        }

        public Integer getPuntaje() {
            return puntaje;
        }

        public void setPuntaje(Integer puntaje) {
            this.puntaje = puntaje;
        }

        public String getComentario() {
            return comentario;
        }

        public void setComentario(String comentario) {
            this.comentario = comentario;
        }
    }

    @GetMapping("/producto/{id}")
    public List<ResenaProductoDTO> getResenasProducto(@PathVariable Integer id) {
        return resenaProductoRepository.findByProducto_Id(id)
                .stream()
                .map(ResenaProductoDTO::new)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> guardarResenaProducto(@RequestBody ResenaProductoRequest request, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuarioLogueado = usuarioRepository.findByEmail(authentication.getName());
        if (usuarioLogueado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (usuarioLogueado.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Los administradores no pueden emitir votos ni reseñas.");
        }

        if (request.getProductoId() == null || request.getPuntaje() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Producto ID y puntaje son requeridos.");
        }

        // VALIDACIÓN B: Comentario obligatorio y longitud mínima de 10 caracteres
        String comentario = request.getComentario();
        if (comentario == null || comentario.trim().length() < 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El comentario es obligatorio y debe tener al menos 10 caracteres.");
        }

        // VALIDACIÓN C: Filtro de groserías
        if (contieneGroserias(comentario)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lenguaje inapropiado detectado.");
        }

        Producto producto = productoRepository.findById(request.getProductoId()).orElse(null);
        if (producto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado.");
        }

        // VALIDACIÓN A: Verifica que el usuario logueado NO sea el dueño del producto
        if (producto.getStand() != null && producto.getStand().getFeriante() != null && producto.getStand().getFeriante().getUsuario() != null) {
            int idDuenoProducto = producto.getStand().getFeriante().getUsuario().getId();
            if (usuarioLogueado.getId() == idDuenoProducto) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No podés calificar tu propio producto.");
            }
        }

        // Valida duplicados
        if (resenaProductoRepository.existsByUsuario_IdAndProducto_Id(usuarioLogueado.getId(), request.getProductoId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya calificaste este producto.");
        }

        ResenaProducto resena = new ResenaProducto(usuarioLogueado, producto, request.getPuntaje(), comentario.trim());
        resenaProductoRepository.save(resena);

        return ResponseEntity.ok("Reseña de producto guardada con éxito");
    }

    @PutMapping("/{id}/responder")
    public ResponseEntity<?> responderResena(@PathVariable Integer id, @RequestBody String textoRespuesta, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuarioLogueado = usuarioRepository.findByEmail(authentication.getName());
        if (usuarioLogueado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (usuarioLogueado.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Los administradores no pueden responder reseñas.");
        }

        ResenaProducto resena = resenaProductoRepository.findById(id).orElse(null);
        if (resena == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reseña no encontrada.");
        }

        // Validación: Verifica que el usuario logueado SEA ESTRICTAMENTE el dueño del producto calificado
        if (resena.getProducto() == null || resena.getProducto().getStand() == null || resena.getProducto().getStand().getFeriante() == null || resena.getProducto().getStand().getFeriante().getUsuario() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede verificar la propiedad del producto.");
        }

        int idDuenoProducto = resena.getProducto().getStand().getFeriante().getUsuario().getId();
        if (usuarioLogueado.getId() != idDuenoProducto) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el dueño del producto puede responder.");
        }

        resena.setRespuesta(textoRespuesta);
        resena.setFechaRespuesta(LocalDateTime.now());
        resenaProductoRepository.save(resena);

        return ResponseEntity.ok("Respuesta guardada/actualizada correctamente");
    }

    @DeleteMapping("/{id}/respuesta")
    public ResponseEntity<?> eliminarRespuesta(@PathVariable Integer id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuarioLogueado = usuarioRepository.findByEmail(authentication.getName());
        if (usuarioLogueado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (usuarioLogueado.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Los administradores no pueden borrar respuestas.");
        }

        ResenaProducto resena = resenaProductoRepository.findById(id).orElse(null);
        if (resena == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reseña no encontrada.");
        }

        if (resena.getProducto() == null || resena.getProducto().getStand() == null || resena.getProducto().getStand().getFeriante() == null || resena.getProducto().getStand().getFeriante().getUsuario() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede verificar la propiedad del producto.");
        }

        int idDuenoProducto = resena.getProducto().getStand().getFeriante().getUsuario().getId();
        if (usuarioLogueado.getId() != idDuenoProducto) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el dueño del producto puede eliminar la respuesta.");
        }

        resena.setRespuesta(null);
        resena.setFechaRespuesta(null);
        resenaProductoRepository.save(resena);

        return ResponseEntity.ok("Respuesta eliminada correctamente");
    }

    private boolean contieneGroserias(String texto) {
        if (texto == null) return false;
        List<String> palabrasProhibidas = Arrays.asList("mierda", "puto", "boludo", "estafa", "hdp", "tarado");
        return palabrasProhibidas.stream().anyMatch(texto.toLowerCase()::contains);
    }
}
