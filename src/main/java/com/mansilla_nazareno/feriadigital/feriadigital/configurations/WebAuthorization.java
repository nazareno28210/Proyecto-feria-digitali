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

        http.csrf(csrf -> csrf.disable())// desactivar CSRF temporalmente para pruebas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/web/index.html", "/web/js/**", "/web/css/**", "/web/img/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login", "/api/logout").permitAll()
                        // Protege rutas de API o dashboard
                        .requestMatchers("/api/**").hasAuthority("ADMIN")
                        .requestMatchers("/web/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/**").hasAuthority("USUARIO")
                        .requestMatchers("/web/**").hasAuthority("USUARIO")
                        .anyRequest().denyAll()
                );

        // Login personalizado
        http.formLogin(form -> form
                .loginPage("/web/login.html") // tu página de login real
                .loginProcessingUrl("/api/login") // endpoint que procesa el POST
                .usernameParameter("email")
                .passwordParameter("password")


                .successHandler((request, response, authentication) -> {
                    clearAuthenticationAttributes(request);
                    response.setStatus(HttpServletResponse.SC_OK); // devuelve 200
                })
                .failureHandler((request, response, exception) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // devuelve 401
                })
        );

        // Logout personalizado
        http.logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
        );

        return http.build();
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}
