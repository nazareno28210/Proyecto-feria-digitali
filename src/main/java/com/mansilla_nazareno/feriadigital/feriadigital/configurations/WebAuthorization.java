package com.mansilla_nazareno.feriadigital.feriadigital.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;

@EnableWebSecurity
@Configuration
public class WebAuthorization {

    /**
     * 🔐 CONFIGURACIÓN PRINCIPAL DE SEGURIDAD
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // 🔒 Endpoint para obtener el usuario logueado
                        .requestMatchers("/api/usuarios/current").authenticated()

                        // 🔒 Endpoint para CAMBIO DE CONTRASEÑA
                        .requestMatchers("/api/password/**").authenticated()

                        // 🌐 Todo lo demás es público
                        .anyRequest().permitAll()
                )

                // 🔑 LOGIN POR FORM (API)
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .usernameParameter("email")
                        .passwordParameter("password")

                        // ✅ Login correcto → 200 OK
                        .successHandler((req, res, auth) ->
                                res.setStatus(HttpServletResponse.SC_OK))

                        // ❌ Login incorrecto → 401
                        .failureHandler((req, res, ex) ->
                                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED))

                        .permitAll()
                )

                // 🚪 LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                        .deleteCookies("JSESSIONID")
                )

                // ❌ No autenticado → 401
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }

    /**
     * 🧠 AuthenticationManager
     * Spring lo usa para autenticar usuarios
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
