package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun.ResenaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Resena;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Feriante.ProductoRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.ResenaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsurioComun.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/producto/{id}")
    public List<ResenaDTO> getResenasProducto(@PathVariable Integer id) {
        return resenaRepository.findByProducto_Id(id)
                .stream().map(ResenaDTO::new).toList();
    }

    @PostMapping
    public ResponseEntity<?> guardarResena(@RequestBody Resena nuevaResena, Authentication authentication) {
        // 1. Verificación de Autenticación
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Usuario usuarioLogueado = usuarioRepository.findByEmail(authentication.getName());
        String comentario = nuevaResena.getComentario();

        // 2. Validación de Longitud (Mín. 10, Máx. 500 caracteres)
        if (comentario == null || comentario.trim().length() < 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El comentario es muy corto (mín. 10 caracteres).");
        }
        if (comentario.length() > 500) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El comentario es muy largo (máx. 500 caracteres).");
        }

        // 3. Filtro de Malas Palabras
        if (contieneGroserias(comentario)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tu comentario contiene lenguaje inapropiado.");
        }

        Producto productoDB = productoRepository.findById(nuevaResena.getProducto().getId()).orElse(null);

        if (productoDB != null) {
            // 4. Bloqueo de Autorreseña
            int idLogueado = usuarioLogueado.getId();
            int idUsuarioDueño = productoDB.getStand().getFeriante().getUsuario().getId();

            if (idLogueado == idUsuarioDueño) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No podés calificar tu propio producto.");
            }

            // Asociación automática de Stand y Feria
            nuevaResena.setStand(productoDB.getStand());
            nuevaResena.setFeria(productoDB.getStand().getFeria());
        }

        // 5. Validación: Una sola reseña por producto
        boolean yaComento = resenaRepository.existsByUsuario_IdAndProducto_Id(
                usuarioLogueado.getId(),
                nuevaResena.getProducto().getId()
        );

        if (yaComento) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya dejaste una opinión sobre este producto.");
        }

        nuevaResena.setUsuario(usuarioLogueado);
        resenaRepository.save(nuevaResena);
        return ResponseEntity.ok("Reseña guardada");
    }

    /**
     * Método auxiliar para filtrar lenguaje inapropiado.
     */
    private boolean contieneGroserias(String texto) {
        List<String> palabrasProhibidas = Arrays.asList("mierda", "puto", "boludo", "estafa", "hdp", "tarado");
        String textoMin = texto.toLowerCase();
        return palabrasProhibidas.stream().anyMatch(textoMin::contains);
    }
}