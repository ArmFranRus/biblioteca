package com.biblioteca.dto;

import java.time.LocalDateTime;

public record RecensioneDTO(
        Long id,
        Long libroId,
        Long utenteId,
        String autore,
        Integer voto,
        String testo,
        LocalDateTime data,
        boolean mia
) {}
