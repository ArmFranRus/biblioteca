package com.biblioteca.config;

import com.biblioteca.security.CustomUserDetailsService;
import com.biblioteca.security.JwtAuthenticationFilter;
import com.biblioteca.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // endpoint pubblici
                        .requestMatchers("/api/auth/**", "/api/ping").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // lettura pubblica del catalogo
                        .requestMatchers(HttpMethod.GET,
                                "/api/books/**", "/api/categories/**", "/api/authors/**").permitAll()
                        // scritture sul catalogo: riservate allo staff
                        .requestMatchers(HttpMethod.POST,
                                "/api/books/**", "/api/categories/**", "/api/authors/**").hasRole("STAFF")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/books/**", "/api/categories/**", "/api/authors/**").hasRole("STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/api/copies/**").hasRole("STAFF")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/books/**", "/api/categories/**", "/api/authors/**", "/api/copies/**").hasRole("STAFF")
                        // gestione prestiti: riservata allo staff (i propri prestiti sono su /api/me/loans)
                        // i propri prestiti: qualsiasi utente autenticato
                        .requestMatchers(HttpMethod.GET, "/api/loans/mine").authenticated()
                        // gestione prestiti: riservata allo staff
                        .requestMatchers("/api/loans/**").hasRole("STAFF")
                        // gestione utenti: riservata allo staff
                        .requestMatchers("/api/users/**").hasRole("STAFF")
                        // prenotazioni: elenco completo allo staff, il resto agli utenti autenticati
                        .requestMatchers(HttpMethod.GET, "/api/reservations/all").hasRole("STAFF")
                        .requestMatchers("/api/reservations/**").authenticated()
                        // recensioni: lettura pubblica, scrittura/eliminazione ad utenti autenticati
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers("/api/reviews/**").authenticated()
                        // dashboard: solo staff
                        .requestMatchers("/api/dashboard/**").hasRole("STAFF")
                        // tutto il resto richiede autenticazione (incluso /api/me/**)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Non autenticato")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
