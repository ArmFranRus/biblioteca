package com.biblioteca.dto;

import java.util.List;

public record DashboardDTO(
        long totaleLibri,
        long totaleCopie,
        long copieDisponibili,
        long totaleUtenti,
        long prestitiAttivi,
        long prestitiInRitardo,
        long prenotazioniInAttesa,
        long prenotazioniPronte,
        List<TitoloPrestitiDTO> titoliPiuPrestati
) {}
