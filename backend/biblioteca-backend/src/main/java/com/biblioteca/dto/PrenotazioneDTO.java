package com.biblioteca.dto;

import com.biblioteca.model.enums.StatoPrenotazione;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PrenotazioneDTO(
        Long id,
        Long libroId,
        String titoloLibro,
        Long utenteId,
        String utenteEmail,
        String utenteNome,
        LocalDateTime data,
        StatoPrenotazione stato,
        LocalDate scadenzaRitiro
) {}
