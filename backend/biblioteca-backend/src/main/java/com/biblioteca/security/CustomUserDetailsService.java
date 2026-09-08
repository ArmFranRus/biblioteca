package com.biblioteca.security;

import com.biblioteca.model.Utente;
import com.biblioteca.repository.UtenteRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtenteRepository utenteRepository;

    public CustomUserDetailsService(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));

        return User.builder()
                .username(utente.getEmail())
                .password(utente.getPasswordHash())
                .authorities(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name()))
                .disabled(!Boolean.TRUE.equals(utente.getAttivo()))
                .build();
    }
}
