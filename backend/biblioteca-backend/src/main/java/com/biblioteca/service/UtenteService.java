package com.biblioteca.service;

import com.biblioteca.dto.StaffCreateRequest;
import com.biblioteca.dto.UtenteDTO;
import com.biblioteca.model.Utente;
import com.biblioteca.model.enums.Ruolo;
import com.biblioteca.repository.UtenteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UtenteDTO> findAll() {
        return utenteRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<UtenteDTO> search(String q) {
        if (q == null || q.isBlank()) {
            return findAll();
        }
        return utenteRepository.search(q.trim()).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public UtenteDTO findById(Long id) {
        return toDTO(getEntity(id));
    }

    @Transactional
    public UtenteDTO createStaff(StaffCreateRequest req) {
        if (utenteRepository.existsByEmail(req.email())) {
            throw new EmailGiaRegistrataException(req.email());
        }
        Utente u = new Utente();
        u.setEmail(req.email());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setNome(req.nome());
        u.setCognome(req.cognome());
        u.setRuolo(Ruolo.STAFF);
        u.setAttivo(true);
        return toDTO(utenteRepository.save(u));
    }

    @Transactional
    public UtenteDTO setAttivo(Long id, boolean attivo) {
        Utente u = getEntity(id);
        u.setAttivo(attivo);
        return toDTO(utenteRepository.save(u));
    }

    private Utente getEntity(Long id) {
        return utenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));
    }

    private UtenteDTO toDTO(Utente u) {
        return new UtenteDTO(u.getId(), u.getEmail(), u.getNome(), u.getCognome(), u.getRuolo(), u.getAttivo());
    }
}
