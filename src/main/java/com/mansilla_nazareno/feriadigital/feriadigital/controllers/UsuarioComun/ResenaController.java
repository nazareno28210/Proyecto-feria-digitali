package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.UsuarioComun.ResenaDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Resena;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
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

    @Autowired
    private StandRepository standRepository; // Necesario para buscar el dueño del stand

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

        // 🛑 SOLUCIÓN FALLO ANTERIOR (2): Bloquear a los Administradores
        if (usuarioLogueado.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Los administradores no pueden emitir votos ni reseñas.");
        }

        int idLogueado = usuarioLogueado.getId();
        String comentarioRaw = nuevaResena.getComentario(); // El comentario que viene del front

        // --- CASO A: RESEÑA DE PRODUCTO ---
        if (nuevaResena.getProducto() != null) {
            if (comentarioRaw == null || comentarioRaw.trim().length() < 10) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El comentario es muy corto (mín. 10 caracteres).");
            }
            if (contieneGroserias(comentarioRaw)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lenguaje inapropiado detectado.");
            }

            Producto productoDB = productoRepository.findById(nuevaResena.getProducto().getId()).orElse(null);
            if (productoDB != null) {
                // Bloqueo de Autoreseña de Producto
                if (idLogueado == productoDB.getStand().getFeriante().getUsuario().getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No podés calificar tu propio producto.");
                }
                // Asociación automática para estadística
                nuevaResena.setStand(productoDB.getStand());
                nuevaResena.setFeria(productoDB.getStand().getFeria());
            }

            if (resenaRepository.existsByUsuario_IdAndProducto_Id(idLogueado, nuevaResena.getProducto().getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya calificaste este producto.");
            }
        }

        // --- CASO B: RESEÑA DE STAND (Aquí están tus dos correcciones) ---
        else if (nuevaResena.getStand() != null) {
            Stand standDB = standRepository.findById(nuevaResena.getStand().getId()).orElse(null);

            if (standDB != null) {
                // 🛑 SOLUCIÓN PROBLEMA 1: Bloqueo de Autocalificación de Stand
                int idUsuarioDueño = standDB.getFeriante().getUsuario().getId();
                if (idLogueado == idUsuarioDueño) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No podés calificar tu propio Stand.");
                }
                // Asociación automática de Feria
                nuevaResena.setFeria(standDB.getFeria());
            }

            // 📝 SOLUCIÓN PROBLEMA 2: Comentario por defecto si viene NULL
            if (comentarioRaw == null || comentarioRaw.trim().isEmpty()) {
                nuevaResena.setComentario("Calificación de Stand");
            } else if (contieneGroserias(comentarioRaw)) {
                // Por si algún día habilitás comentarios en stands, dejamos el filtro
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lenguaje inapropiado detectado.");
            }

            // 🛠️ CORRECCIÓN FALLO ANTERIOR (3): Usamos el método "Puro"
            if (resenaRepository.existsByUsuario_IdAndStand_IdPuro(idLogueado, nuevaResena.getStand().getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya calificaste este Stand.");
            }
        }

        // --- CASO C: VOTO DE FERIA ---
        else if (nuevaResena.getFeria() != null) {
            // En Feria el comentario siempre es genérico
            nuevaResena.setComentario("Voto de Feria");

            // 🛠️ CORRECCIÓN FALLO ANTERIOR (1): Usamos el método "Puro"
            if (resenaRepository.existsByUsuario_IdAndFeria_IdPuro(idLogueado, nuevaResena.getFeria().getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya votaste en esta Feria.");
            }
        }

        // Guardado Final
        nuevaResena.setUsuario(usuarioLogueado);
        resenaRepository.save(nuevaResena);
        return ResponseEntity.ok("Guardado con éxito");
    }

    private boolean contieneGroserias(String texto) {
        if (texto == null) return false;
        List<String> palabrasProhibidas = Arrays.asList("mierda", "puto", "boludo", "estafa", "hdp", "tarado");
        return palabrasProhibidas.stream().anyMatch(texto.toLowerCase()::contains);
    }
}