package com.biblioteca.service;

import com.biblioteca.dto.RecensioneDTO;
import com.biblioteca.dto.RecensioneRequest;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Recensione;
import com.biblioteca.model.Utente;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.RecensioneRepository;
import com.biblioteca.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecensioneService {

    private final RecensioneRepository recensioneRepository;
    private final LibroRepository libroRepository;
    private final UtenteRepository utenteRepository;

    public RecensioneService(RecensioneRepository recensioneRepository,
                             LibroRepository libroRepository,
                             UtenteRepository utenteRepository) {
        this.recensioneRepository = recensioneRepository;
        this.libroRepository = libroRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional
    public RecensioneDTO crea(RecensioneRequest req, String email) {
        Libro libro = libroRepository.findById(req.libroId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro", req.libroId()));
        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", email));

        if (recensioneRepository.existsByLibroIdAndUtenteId(libro.getId(), utente.getId())) {
            throw new ConflittoException("Hai gia' recensito questo libro.");
        }

        Recensione r = new Recensione();
        r.setLibro(libro);
        r.setUtente(utente);
        r.setVoto(req.voto());
        r.setTesto(req.testo());
        recensioneRepository.save(r);
        return toDTO(r, email);
    }

    @Transactional(readOnly = true)
    public List<RecensioneDTO> listaPerLibro(Long libroId, String emailCorrente) {
        if (!libroRepository.existsById(libroId)) {
            throw new ResourceNotFoundException("Libro", libroId);
        }
        return recensioneRepository.findByLibroIdOrderByDataDesc(libroId)
                .stream().map(r -> toDTO(r, emailCorrente)).toList();
    }

    @Transactional
    public void elimina(Long id, String email, boolean staff) {
        Recensione r = recensioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recensione", id));
        if (!staff && !r.getUtente().getEmail().equals(email)) {
            throw new AccessNegatoException("Puoi eliminare solo le tue recensioni.");
        }
        recensioneRepository.delete(r);
    }

    private RecensioneDTO toDTO(Recensione r, String emailCorrente) {
        Utente u = r.getUtente();
        String nome = (u.getNome() == null ? "" : u.getNome());
        String cognome = (u.getCognome() == null ? "" : u.getCognome());
        String autore = (nome + " " + cognome).trim();
        if (autore.isEmpty()) {
            autore = "Utente";
        }
        boolean mia = emailCorrente != null && u.getEmail().equals(emailCorrente);
        return new RecensioneDTO(
                r.getId(), r.getLibro().getId(), u.getId(), autore,
                r.getVoto(), r.getTesto(), r.getData(), mia);
    }
}
