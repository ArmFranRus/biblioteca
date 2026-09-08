package com.biblioteca.service;

import com.biblioteca.dto.PrestitoDTO;
import com.biblioteca.dto.PrestitoRequest;
import com.biblioteca.model.Copia;
import com.biblioteca.model.Prestito;
import com.biblioteca.model.Utente;
import com.biblioteca.model.enums.StatoCopia;
import com.biblioteca.model.enums.StatoPrestito;
import com.biblioteca.repository.CopiaRepository;
import com.biblioteca.repository.PrestitoRepository;
import com.biblioteca.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PrestitoService {

    private static final List<StatoPrestito> STATI_ATTIVI =
            List.of(StatoPrestito.ATTIVO, StatoPrestito.IN_RITARDO);

    private final PrestitoRepository prestitoRepository;
    private final CopiaRepository copiaRepository;
    private final UtenteRepository utenteRepository;
    private final PrenotazioneService prenotazioneService;
    private final int maxAttivi;

    public PrestitoService(PrestitoRepository prestitoRepository,
                           CopiaRepository copiaRepository,
                           UtenteRepository utenteRepository,
                           PrenotazioneService prenotazioneService,
                           @Value("${app.loan.max-active-per-user:5}") int maxAttivi) {
        this.prestitoRepository = prestitoRepository;
        this.copiaRepository = copiaRepository;
        this.utenteRepository = utenteRepository;
        this.prenotazioneService = prenotazioneService;
        this.maxAttivi = maxAttivi;
    }

    @Transactional
    public PrestitoDTO registra(PrestitoRequest req) {
        Copia copia = copiaRepository.findById(req.copiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Copia", req.copiaId()));
        if (copia.getStato() != StatoCopia.DISPONIBILE) {
            throw new ConflittoException("La copia non e' disponibile per il prestito.");
        }

        Utente utente = utenteRepository.findById(req.utenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Utente", req.utenteId()));
        if (!Boolean.TRUE.equals(utente.getAttivo())) {
            throw new ConflittoException("L'utente non e' attivo.");
        }

        Long libroId = copia.getLibro().getId();

        // Non prestare a un utente qualsiasi le copie riservate a chi e' in attesa di ritiro.
        long disponibili = copiaRepository.countByLibroIdAndStato(libroId, StatoCopia.DISPONIBILE);
        long pronteAltri = prenotazioneService.contaPronteDiAltri(libroId, utente.getId());
        if (disponibili <= pronteAltri) {
            throw new ConflittoException(
                    "Le copie disponibili di questo titolo sono riservate a utenti in attesa di ritiro.");
        }

        long attivi = prestitoRepository.countByUtenteIdAndStatoIn(utente.getId(), STATI_ATTIVI);
        if (attivi >= maxAttivi) {
            throw new ConflittoException(
                    "Limite di " + maxAttivi + " prestiti attivi raggiunto per l'utente.");
        }

        LocalDate oggi = LocalDate.now();
        Prestito prestito = new Prestito();
        prestito.setCopia(copia);
        prestito.setUtente(utente);
        prestito.setDataPrestito(oggi);
        prestito.setDataScadenza(oggi.plusMonths(1));
        prestito.setStato(StatoPrestito.ATTIVO);
        prestitoRepository.save(prestito);

        copia.setStato(StatoCopia.PRESTATA);
        copiaRepository.save(copia);

        // se questo prestito soddisfa una prenotazione pronta dell'utente, la si marca evasa
        prenotazioneService.evadiSePronta(libroId, utente.getId());

        return toDTO(prestito);
    }

    @Transactional
    public PrestitoDTO registraRestituzione(Long id) {
        Prestito prestito = prestitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestito", id));
        if (prestito.getDataRestituzione() != null) {
            throw new ConflittoException("Il prestito risulta gia' restituito.");
        }

        prestito.setDataRestituzione(LocalDate.now());
        prestito.setStato(StatoPrestito.RESTITUITO);
        prestitoRepository.save(prestito);

        Copia copia = prestito.getCopia();
        copia.setStato(StatoCopia.DISPONIBILE);
        copiaRepository.save(copia);

        // evasione della coda: se qualcuno e' in attesa del titolo, diventa "pronto al ritiro"
        prenotazioneService.promuoviCoda(copia.getLibro().getId());

        return toDTO(prestito);
    }

    @Transactional(readOnly = true)
    public List<PrestitoDTO> lista(Long utenteId, StatoPrestito stato) {
        List<Prestito> prestiti;
        if (utenteId != null) {
            prestiti = prestitoRepository.findByUtenteIdOrderByDataPrestitoDesc(utenteId);
        } else if (stato != null) {
            prestiti = prestitoRepository.findByStatoOrderByDataPrestitoDesc(stato);
        } else {
            prestiti = prestitoRepository.findAllByOrderByDataPrestitoDesc();
        }
        return prestiti.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PrestitoDTO> listaPerEmail(String email) {
        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", email));
        return prestitoRepository.findByUtenteIdOrderByDataPrestitoDesc(utente.getId())
                .stream().map(this::toDTO).toList();
    }

    private PrestitoDTO toDTO(Prestito p) {
        Copia copia = p.getCopia();
        Utente u = p.getUtente();
        LocalDate oggi = LocalDate.now();

        boolean inRitardo = p.getDataRestituzione() == null && oggi.isAfter(p.getDataScadenza());
        long giorniRitardo = inRitardo ? ChronoUnit.DAYS.between(p.getDataScadenza(), oggi) : 0;

        String nome = (u.getNome() == null ? "" : u.getNome());
        String cognome = (u.getCognome() == null ? "" : u.getCognome());

        return new PrestitoDTO(
                p.getId(),
                copia.getId(),
                copia.getCodiceInventario(),
                copia.getLibro().getId(),
                copia.getLibro().getTitolo(),
                u.getId(),
                u.getEmail(),
                (nome + " " + cognome).trim(),
                p.getDataPrestito(),
                p.getDataScadenza(),
                p.getDataRestituzione(),
                p.getStato(),
                inRitardo,
                giorniRitardo
        );
    }
}
