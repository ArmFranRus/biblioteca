package com.biblioteca.service;

import com.biblioteca.dto.AuthResponse;
import com.biblioteca.dto.LoginRequest;
import com.biblioteca.dto.RegisterRequest;
import com.biblioteca.model.Utente;
import com.biblioteca.model.enums.Ruolo;
import com.biblioteca.repository.UtenteRepository;
import com.biblioteca.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UtenteRepository utenteRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * La registrazione pubblica crea sempre e solo utenti con ruolo PUBLIC.
     * Gli account STAFF sono creati esclusivamente da altro personale.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (utenteRepository.existsByEmail(request.email())) {
            throw new EmailGiaRegistrataException(request.email());
        }

        Utente utente = new Utente();
        utente.setEmail(request.email());
        utente.setPasswordHash(passwordEncoder.encode(request.password()));
        utente.setNome(request.nome());
        utente.setCognome(request.cognome());
        utente.setRuolo(Ruolo.PUBLIC);
        utente.setAttivo(true);
        utenteRepository.save(utente);

        String token = jwtService.generateToken(utente.getEmail(), utente.getRuolo().name());
        return new AuthResponse(token, utente.getEmail(), utente.getRuolo().name());
    }

    public AuthResponse login(LoginRequest request) {
        // Lancia BadCredentialsException / DisabledException in caso di problemi.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Utente utente = utenteRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.generateToken(utente.getEmail(), utente.getRuolo().name());
        return new AuthResponse(token, utente.getEmail(), utente.getRuolo().name());
    }
}
