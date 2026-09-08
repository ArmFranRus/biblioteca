package com.biblioteca.dto;

import java.util.List;

public record LibroDTO(
        Long id,
        String titolo,
        String isbn,
        String editore,
        Integer anno,
        CategoriaDTO categoria,
        List<AutoreDTO> autori,
        long copieTotali,
        long copieDisponibili,
        Double votoMedio,
        long numeroRecensioni
) {}
