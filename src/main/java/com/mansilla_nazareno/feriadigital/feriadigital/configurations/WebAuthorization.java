package com.mansilla_nazareno.feriadigital.feriadigital.configurations;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@EnableWebSecurity
@Configuration
public class WebAuthorization {

    @Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Todo el sitio es público
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .disable() // 🚫 Desactiva el login por formulario
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                );

        return http.build();
    }
    //.requestMatchers("/web/admin/**", "/api/admin/**").hasRole("ADMINISTRADOR")
    //                        .requestMatchers("/web/feriante/**", "/api/feriante/**").hasRole("FERIANTE")
    //                        .requestMatchers("/web/usuario/**", "/api/usuario/**").hasRole("NORMAL")
    //                        .anyRequest().authenticated()
    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}
