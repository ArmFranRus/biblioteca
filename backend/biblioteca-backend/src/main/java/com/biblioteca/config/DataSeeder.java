package com.biblioteca.config;

import com.biblioteca.model.Utente;
import com.biblioteca.model.enums.Ruolo;
import com.biblioteca.repository.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea un utente STAFF di default al primo avvio, se non esiste gia.
 * L'operazione e' idempotente: agli avvii successivi l'utente viene trovato
 * e non viene eseguita alcuna azione (nessun cambio password richiesto).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(UtenteRepository utenteRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.admin.email}") String adminEmail,
                      @Value("${app.admin.password}") String adminPassword) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (utenteRepository.existsByEmail(adminEmail)) {
            log.info("Utente admin gia' presente ({}): nessuna azione.", adminEmail);
            return;
        }
        Utente admin = new Utente();
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRuolo(Ruolo.STAFF);
        admin.setNome("Amministratore");
        admin.setCognome("Biblioteca");
        admin.setAttivo(true);
        utenteRepository.save(admin);
        log.info("Utente admin STAFF creato: {}", adminEmail);
    }
}
