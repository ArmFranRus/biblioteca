package com.biblioteca.config;

import com.biblioteca.service.PrenotazioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Ogni giorno fa scadere le prenotazioni PRONTA non ritirate entro il termine
 * e promuove il prossimo utente in coda per il titolo.
 */
@Component
public class PrenotazioneScheduler {

    private static final Logger log = LoggerFactory.getLogger(PrenotazioneScheduler.class);

    private final PrenotazioneService prenotazioneService;

    public PrenotazioneScheduler(PrenotazioneService prenotazioneService) {
        this.prenotazioneService = prenotazioneService;
    }

    @Scheduled(cron = "0 30 1 * * *")
    public void scadenzaRitiri() {
        int scadute = prenotazioneService.scadiNonRitirate();
        if (scadute > 0) {
            log.info("Prenotazioni scadute per mancato ritiro: {}", scadute);
        }
    }
}
