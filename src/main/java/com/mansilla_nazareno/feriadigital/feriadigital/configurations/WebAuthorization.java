package com.mansilla_nazareno.feriadigital.feriadigital.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

// ⬇️ CAMBIO 1: Importación actualizada a 'jakarta' ⬇️
import jakarta.servlet.http.HttpServletResponse;

@EnableWebSecurity
@Configuration
public class WebAuthorization {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. LA RUTA CLAVE: Protegemos solo la API que nos dice quiénes somos.
                        //    Tu login.js la llama justo después de loguearse.

                        // ⬇️ CAMBIO 2: 'antMatchers' renombrado a 'requestMatchers' ⬇️
                        .requestMatchers("/api/usuarios/current").authenticated()

                        // 2. TODO LO DEMÁS: Es público por ahora (tu Goal 1)
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        // 3. HABILITAMOS EL LOGIN (tu Goal 2)
                        .loginProcessingUrl("/api/login")
                        .usernameParameter("email")
                        .passwordParameter("password")

                        // 4. Lo configuramos para API (devuelve 200 OK en éxito)
                        .successHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))

                        // 5. Devuelve 401 en fallo (para el 'catch' de Axios)
                        .failureHandler((req, res, ex) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED))

                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                        .deleteCookies("JSESSIONID")
                )
                .exceptionHandling(ex -> ex
                        // 6. Si Axios llama a /api/usuarios/current sin cookie, devuelve 401
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }
}