package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Stand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.ResenaStand;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.StandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.ResenaStandRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resenas-stand")
public class ResenaStandController {

    @Autowired
    private ResenaStandRepository resenaStandRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private StandRepository standRepository;

    public static class ResenaStandRequest {
        @JsonProperty("stand_id")
        private Integer standId;

        private Integer puntaje;

        public Integer getStandId() {
            return standId;
        }

        public void setStandId(Integer standId) {
            this.standId = standId;
        }

        public Integer getPuntaje() {
            return puntaje;
        }

        public void setPuntaje(Integer puntaje) {
            this.puntaje = puntaje;
        }
    }

    @PostMapping
    public ResponseEntity<?> guardarResenaStand(@RequestBody ResenaStandRequest request, Authentication authentication) {
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

        if (request.getStandId() == null || request.getPuntaje() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stand ID y puntaje son requeridos.");
        }

        if (request.getPuntaje() < 1 || request.getPuntaje() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El puntaje debe ser entre 1 y 5.");
        }

        Stand stand = standRepository.findById(request.getStandId()).orElse(null);
        if (stand == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Stand no encontrado.");
        }

        // Validación de propiedad (el dueño del stand no puede calificar su propio stand)
        if (stand.getFeriante() != null && stand.getFeriante().getUsuario() != null) {
            int idUsuarioDueno = stand.getFeriante().getUsuario().getId();
            if (usuarioLogueado.getId() == idUsuarioDueno) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No podés calificar tu propio Stand.");
            }
        }

        if (resenaStandRepository.existsByUsuario_IdAndStand_Id(usuarioLogueado.getId(), request.getStandId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya calificaste este Stand.");
        }

        ResenaStand resena = new ResenaStand(usuarioLogueado, stand, request.getPuntaje());
        resenaStandRepository.save(resena);

        return ResponseEntity.ok("Reseña de Stand guardada con éxito");
    }
}
