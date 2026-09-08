package com.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record LibroRequest(
        @NotBlank String titolo,
        String isbn,
        String editore,
        Integer anno,
        Long categoriaId,
        List<Long> autoriIds
) {}
