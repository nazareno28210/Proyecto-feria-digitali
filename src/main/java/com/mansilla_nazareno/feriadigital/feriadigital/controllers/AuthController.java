package com.mansilla_nazareno.feriadigital.feriadigital.controllers;

import com.mansilla_nazareno.feriadigital.feriadigital.dtos.ResetPasswordDTO;
import com.mansilla_nazareno.feriadigital.feriadigital.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/verificar")
    public ResponseEntity<Void> verificarCuenta(@RequestParam String token) {
        boolean verificado = authService.verificarCuenta(token);
        String redirectUrl = verificado ? "/web/login.html?verificado=true" : "/web/login.html?errorVerificacion=true";
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        authService.generarTokenRecuperacion(email);
        return ResponseEntity.ok("Si el correo existe, se enviará un link de recuperación.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        return authService.resetearPassword(dto.getToken(), dto.getNuevaPassword());
    }
}