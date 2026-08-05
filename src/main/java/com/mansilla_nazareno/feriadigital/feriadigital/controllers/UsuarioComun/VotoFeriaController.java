package com.mansilla_nazareno.feriadigital.feriadigital.controllers.UsuarioComun;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Feria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.VotoFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.FeriaRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.UsuarioRepository;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.VotoFeriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votos-feria")
public class VotoFeriaController {

    @Autowired
    private VotoFeriaRepository votoFeriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FeriaRepository feriaRepository;

    public static class VotoFeriaRequest {
        @JsonProperty("feria_id")
        private Integer feriaId;

        @JsonProperty("esPositivo")
        private Boolean esPositivo;

        public Integer getFeriaId() {
            return feriaId;
        }

        public void setFeriaId(Integer feriaId) {
            this.feriaId = feriaId;
        }

        public Boolean getEsPositivo() {
            return esPositivo;
        }

        public void setEsPositivo(Boolean esPositivo) {
            this.esPositivo = esPositivo;
        }
    }

    @PostMapping
    public ResponseEntity<?> guardarVoto(@RequestBody VotoFeriaRequest request, Authentication authentication) {
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

        if (request.getFeriaId() == null || request.getEsPositivo() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Feria ID y sentido del voto son requeridos.");
        }

        Feria feria = feriaRepository.findById(request.getFeriaId()).orElse(null);
        if (feria == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Feria no encontrada.");
        }

        if (votoFeriaRepository.existsByUsuario_IdAndFeria_Id(usuarioLogueado.getId(), request.getFeriaId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya votaste en esta Feria.");
        }

        VotoFeria voto = new VotoFeria(usuarioLogueado, feria, request.getEsPositivo());
        votoFeriaRepository.save(voto);

        return ResponseEntity.ok("Voto guardado con éxito");
    }
}
