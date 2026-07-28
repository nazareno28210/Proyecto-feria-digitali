package com.mansilla_nazareno.feriadigital.feriadigital.configurations;

import com.mansilla_nazareno.feriadigital.feriadigital.models.EstadoUsuario;
import com.mansilla_nazareno.feriadigital.feriadigital.models.UsuarioComun.Usuario;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.UsuarioComun.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebAuthentication extends GlobalAuthenticationConfigurerAdapter {
    @Autowired
    UsuarioRepository usuarioRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inputName -> {
            Usuario usuario = usuarioRepository.findByEmail(inputName);

            if (usuario == null) {
                throw new UsernameNotFoundException("Usuario desconocido: " + inputName);
            }

            // Asigna rol según tipoUsuario
            String rol = usuario.getTipoUsuario().name(); // NORMAL, FERIANTE, ADMINISTRADOR

            boolean activo = usuario.isEnabled() && usuario.getEstadoUsuario() == EstadoUsuario.ACTIVO;

            return new User(
                    usuario.getEmail(),
                    usuario.getContrasena(),
                    activo,
                    true,
                    true,
                    true,
                    AuthorityUtils.createAuthorityList("ROLE_" + rol)
            );
        });
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
