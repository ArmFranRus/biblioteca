package com.biblioteca.service;

import com.biblioteca.dto.DashboardDTO;
import com.biblioteca.dto.TitoloPrestitiDTO;
import com.biblioteca.model.enums.StatoCopia;
import com.biblioteca.model.enums.StatoPrenotazione;
import com.biblioteca.model.enums.StatoPrestito;
import com.biblioteca.repository.CopiaRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrenotazioneRepository;
import com.biblioteca.repository.PrestitoRepository;
import com.biblioteca.repository.UtenteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final LibroRepository libroRepository;
    private final CopiaRepository copiaRepository;
    private final UtenteRepository utenteRepository;
    private final PrestitoRepository prestitoRepository;
    private final PrenotazioneRepository prenotazioneRepository;

    public DashboardService(LibroRepository libroRepository,
                            CopiaRepository copiaRepository,
                            UtenteRepository utenteRepository,
                            PrestitoRepository prestitoRepository,
                            PrenotazioneRepository prenotazioneRepository) {
        this.libroRepository = libroRepository;
        this.copiaRepository = copiaRepository;
        this.utenteRepository = utenteRepository;
        this.prestitoRepository = prestitoRepository;
        this.prenotazioneRepository = prenotazioneRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDTO build() {
        long totaleLibri = libroRepository.count();
        long totaleCopie = copiaRepository.count();
        long copieDisponibili = copiaRepository.countByStato(StatoCopia.DISPONIBILE);
        long totaleUtenti = utenteRepository.count();

        long prestitiAttivi = prestitoRepository.countByStatoIn(
                List.of(StatoPrestito.ATTIVO, StatoPrestito.IN_RITARDO));
        long prestitiInRitardo = prestitoRepository
                .countByDataRestituzioneIsNullAndDataScadenzaBefore(LocalDate.now());

        long prenotazioniInAttesa = prenotazioneRepository.countByStato(StatoPrenotazione.IN_ATTESA);
        long prenotazioniPronte = prenotazioneRepository.countByStato(StatoPrenotazione.PRONTA);

        List<TitoloPrestitiDTO> titoliPiuPrestati = prestitoRepository
                .titoliPiuPrestati(PageRequest.of(0, 5))
                .stream()
                .map(r -> new TitoloPrestitiDTO((Long) r[0], (String) r[1], ((Number) r[2]).longValue()))
                .toList();

        return new DashboardDTO(
                totaleLibri, totaleCopie, copieDisponibili, totaleUtenti,
                prestitiAttivi, prestitiInRitardo,
                prenotazioniInAttesa, prenotazioniPronte,
                titoliPiuPrestati);
    }
}
