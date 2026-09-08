package com.biblioteca.config;

import com.biblioteca.model.Prestito;
import com.biblioteca.model.enums.StatoPrestito;
import com.biblioteca.repository.PrestitoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Ogni giorno segna come IN_RITARDO i prestiti ATTIVI la cui scadenza e' passata.
 * Lo stato serve alle query e alla dashboard; l'interfaccia riceve comunque
 * un flag "inRitardo" calcolato al volo, quindi la correttezza e' immediata anche
 * prima dell'esecuzione pianificata.
 */
@Component
public class PrestitoScheduler {

    private static final Logger log = LoggerFactory.getLogger(PrestitoScheduler.class);

    private final PrestitoRepository prestitoRepository;

    public PrestitoScheduler(PrestitoRepository prestitoRepository) {
        this.prestitoRepository = prestitoRepository;
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void aggiornaRitardi() {
        List<Prestito> scaduti = prestitoRepository
                .findByStatoAndDataScadenzaBefore(StatoPrestito.ATTIVO, LocalDate.now());
        if (scaduti.isEmpty()) {
            return;
        }
        scaduti.forEach(p -> p.setStato(StatoPrestito.IN_RITARDO));
        prestitoRepository.saveAll(scaduti);
        log.info("Aggiornati {} prestiti a IN_RITARDO.", scaduti.size());
    }
}
