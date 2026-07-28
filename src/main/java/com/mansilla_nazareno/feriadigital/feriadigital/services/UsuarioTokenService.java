package com.mansilla_nazareno.feriadigital.feriadigital.services;

import com.mansilla_nazareno.feriadigital.feriadigital.models.TipoToken;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioToken;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioTokenService {

    private final UsuarioTokenRepository usuarioTokenRepository;

    public UsuarioTokenService(UsuarioTokenRepository usuarioTokenRepository) {
        this.usuarioTokenRepository = usuarioTokenRepository;
    }

    @Transactional
    public UsuarioToken generarToken(Usuario usuario, TipoToken tipoToken) {
        // Invalidar tokens previos no usados del mismo tipo para este usuario
        List<UsuarioToken> tokensAnteriores = usuarioTokenRepository
                .findByUsuarioAndTipoTokenAndUsadoFalse(usuario, tipoToken);

        for (UsuarioToken t : tokensAnteriores) {
            t.setUsado(true);
            t.setFechaUso(LocalDateTime.now());
        }
        usuarioTokenRepository.saveAll(tokensAnteriores);

        // Generar nuevo token
        String tokenStr = UUID.randomUUID().toString();
        UsuarioToken nuevoToken = new UsuarioToken(tokenStr, tipoToken, usuario);

        return usuarioTokenRepository.save(nuevoToken);
    }

    public Optional<UsuarioToken> validarToken(String tokenStr, TipoToken tipoToken) {
        Optional<UsuarioToken> tokenOpt = usuarioTokenRepository.findByTokenAndTipoToken(tokenStr, tipoToken);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        UsuarioToken usuarioToken = tokenOpt.get();

        if (!usuarioToken.isValido()) {
            return Optional.empty();
        }

        return Optional.of(usuarioToken);
    }

    @Transactional
    public void marcarComoUsado(UsuarioToken usuarioToken) {
        usuarioToken.setUsado(true);
        usuarioToken.setFechaUso(LocalDateTime.now());
        usuarioTokenRepository.save(usuarioToken);
    }
}
