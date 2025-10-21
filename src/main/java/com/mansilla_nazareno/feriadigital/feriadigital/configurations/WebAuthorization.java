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
                        .requestMatchers("/","/web/index.html","/web/login.html", "/web/js/**", "/web/css/**", "/web/img/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login", "/api/logout").permitAll()


                        .requestMatchers("/web/admin/**", "/api/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/web/feriante/**", "/api/feriante/**").hasRole("FERIANTE")
                        .requestMatchers("/web/usuario/**", "/api/usuario/**").hasRole("NORMAL")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form

                        .usernameParameter("email")
                        .passwordParameter("password")
                        .loginPage("/web/login.html")
                        .loginProcessingUrl("/api/login")

                        .successHandler((request, response, authentication) -> {
                            clearAuthenticationAttributes(request);

                            var authorities = authentication.getAuthorities();
                            String rol = authorities.iterator().next().getAuthority();

                            // Redirección según rol
                            switch (rol) {
                                case "ROLE_ADMINISTRADOR" -> response.sendRedirect("/web/admin.html");
                                case "ROLE_FERIANTE" -> response.sendRedirect("/web/feriante.html");
                                case "ROLE_NORMAL" -> response.sendRedirect("/web/usuario.html");
                                default -> response.sendRedirect("/web/index.html");
                            }
                        })
                        .failureHandler((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .logout(logout -> logout
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
