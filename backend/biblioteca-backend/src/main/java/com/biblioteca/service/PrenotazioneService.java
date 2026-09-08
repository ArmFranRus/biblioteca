package com.biblioteca.service;

import com.biblioteca.dto.PrenotazioneDTO;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prenotazione;
import com.biblioteca.model.Utente;
import com.biblioteca.model.enums.StatoCopia;
import com.biblioteca.model.enums.StatoPrenotazione;
import com.biblioteca.repository.CopiaRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrenotazioneRepository;
import com.biblioteca.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrenotazioneService {

    private static final List<StatoPrenotazione> ATTIVE =
            List.of(StatoPrenotazione.IN_ATTESA, StatoPrenotazione.PRONTA);

    private final PrenotazioneRepository prenotazioneRepository;
    private final CopiaRepository copiaRepository;
    private final LibroRepository libroRepository;
    private final UtenteRepository utenteRepository;
    private final int giorniRitiro;

    public PrenotazioneService(PrenotazioneRepository prenotazioneRepository,
                               CopiaRepository copiaRepository,
                               LibroRepository libroRepository,
                               UtenteRepository utenteRepository,
                               @Value("${app.reservation.pickup-days:3}") int giorniRitiro) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.copiaRepository = copiaRepository;
        this.libroRepository = libroRepository;
        this.utenteRepository = utenteRepository;
        this.giorniRitiro = giorniRitiro;
    }

    @Transactional
    public PrenotazioneDTO prenota(Long libroId, String email) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", libroId));
        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", email));

        long disponibili = copiaRepository.countByLibroIdAndStato(libroId, StatoCopia.DISPONIBILE);
        if (disponibili > 0) {
            throw new ConflittoException("Ci sono copie disponibili: e' possibile il prestito diretto.");
        }
        if (prenotazioneRepository.existsByUtenteIdAndLibroIdAndStatoIn(utente.getId(), libroId, ATTIVE)) {
            throw new ConflittoException("Hai gia' una prenotazione attiva per questo titolo.");
        }

        Prenotazione p = new Prenotazione();
        p.setLibro(libro);
        p.setUtente(utente);
        p.setStato(StatoPrenotazione.IN_ATTESA);
        prenotazioneRepository.save(p);
        return toDTO(p);
    }

    @Transactional
    public void annulla(Long id, String email) {
        Prenotazione p = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione", id));
        if (!p.getUtente().getEmail().equals(email)) {
            throw new AccessNegatoException("Non puoi annullare una prenotazione non tua.");
        }
        if (p.getStato() != StatoPrenotazione.IN_ATTESA && p.getStato() != StatoPrenotazione.PRONTA) {
            throw new ConflittoException("La prenotazione non e' annullabile nel suo stato attuale.");
        }
        StatoPrenotazione precedente = p.getStato();
        p.setStato(StatoPrenotazione.ANNULLATA);
        prenotazioneRepository.save(p);

        // se era "pronta", il posto passa al prossimo in coda
        if (precedente == StatoPrenotazione.PRONTA) {
            promuoviCoda(p.getLibro().getId());
        }
    }

    @Transactional(readOnly = true)
    public List<PrenotazioneDTO> listaPerEmail(String email) {
        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", email));
        return prenotazioneRepository.findByUtenteIdOrderByDataDesc(utente.getId())
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PrenotazioneDTO> lista(StatoPrenotazione stato) {
        List<Prenotazione> prenotazioni = (stato != null)
                ? prenotazioneRepository.findByStatoOrderByDataDesc(stato)
                : prenotazioneRepository.findAllByOrderByDataDesc();
        return prenotazioni.stream().map(this::toDTO).toList();
    }

    // ---- Operazioni usate dal flusso prestiti / scheduler ----

    /** Promuove a PRONTA la prima prenotazione in attesa del titolo, se esiste. */
    @Transactional
    public void promuoviCoda(Long libroId) {
        prenotazioneRepository
                .findFirstByLibroIdAndStatoOrderByDataAsc(libroId, StatoPrenotazione.IN_ATTESA)
                .ifPresent(p -> {
                    p.setStato(StatoPrenotazione.PRONTA);
                    p.setScadenzaRitiro(LocalDate.now().plusDays(giorniRitiro));
                    prenotazioneRepository.save(p);
                });
    }

    /** Se l'utente ha una prenotazione PRONTA per il titolo, la marca EVASA. */
    @Transactional
    public void evadiSePronta(Long libroId, Long utenteId) {
        prenotazioneRepository
                .findFirstByLibroIdAndUtenteIdAndStatoOrderByDataAsc(libroId, utenteId, StatoPrenotazione.PRONTA)
                .ifPresent(p -> {
                    p.setStato(StatoPrenotazione.EVASA);
                    prenotazioneRepository.save(p);
                });
    }

    /** Numero di prenotazioni PRONTA del titolo intestate ad altri utenti. */
    @Transactional(readOnly = true)
    public long contaPronteDiAltri(Long libroId, Long utenteId) {
        return prenotazioneRepository.countByLibroIdAndStatoAndUtenteIdNot(
                libroId, StatoPrenotazione.PRONTA, utenteId);
    }

    /** Fa scadere le prenotazioni PRONTA non ritirate e promuove il prossimo in coda. */
    @Transactional
    public int scadiNonRitirate() {
        List<Prenotazione> scadute = prenotazioneRepository
                .findByStatoAndScadenzaRitiroBefore(StatoPrenotazione.PRONTA, LocalDate.now());
        scadute.forEach(p -> p.setStato(StatoPrenotazione.SCADUTA));
        prenotazioneRepository.saveAll(scadute);
        scadute.stream()
                .map(p -> p.getLibro().getId())
                .distinct()
                .forEach(this::promuoviCoda);
        return scadute.size();
    }

    private PrenotazioneDTO toDTO(Prenotazione p) {
        Utente u = p.getUtente();
        String nome = (u.getNome() == null ? "" : u.getNome());
        String cognome = (u.getCognome() == null ? "" : u.getCognome());
        return new PrenotazioneDTO(
                p.getId(),
                p.getLibro().getId(),
                p.getLibro().getTitolo(),
                u.getId(),
                u.getEmail(),
                (nome + " " + cognome).trim(),
                p.getData(),
                p.getStato(),
                p.getScadenzaRitiro()
        );
    }
}
