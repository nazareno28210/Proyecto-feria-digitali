package com.mansilla_nazareno.feriadigital.feriadigital.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
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

                        // 🛡️ AGREGÁ ESTA LÍNEA PARA LAS RESEÑAS
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/resenas").authenticated()

                        // 🌐 Productos públicos
                        .requestMatchers("/api/productos/publicos").permitAll()

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

                        // ❌ Login incorrecto: Diferenciar contraseña correcta (cuenta inactiva) de credenciales inválidas
                        .failureHandler((req, res, ex) -> {
                            res.setContentType("application/json;charset=UTF-8");
                            if (ex instanceof DisabledException) {
                                res.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
                                res.getWriter().write("{\"error\": \"CUENTA_INACTIVA\", \"message\": \"Debes ingresar a tu correo electrónico y verificar tu cuenta para activarla.\"}");
                            } else {
                                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                                res.getWriter().write("{\"error\": \"CREDENCIALES_INCORRECTAS\", \"message\": \"Correo o contraseña incorrectos.\"}");
                            }
                        })

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
