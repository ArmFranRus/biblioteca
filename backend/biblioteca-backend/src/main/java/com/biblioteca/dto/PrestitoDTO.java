package com.biblioteca.dto;

import com.biblioteca.model.enums.StatoPrestito;

import java.time.LocalDate;

public record PrestitoDTO(
        Long id,
        Long copiaId,
        String codiceInventario,
        Long libroId,
        String titoloLibro,
        Long utenteId,
        String utenteEmail,
        String utenteNome,
        LocalDate dataPrestito,
        LocalDate dataScadenza,
        LocalDate dataRestituzione,
        StatoPrestito stato,
        boolean inRitardo,
        long giorniRitardo
) {}
