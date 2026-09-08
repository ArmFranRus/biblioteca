package com.biblioteca.dto;

import java.util.List;

public record RecensioniResponse(
        double votoMedio,
        long numero,
        List<RecensioneDTO> recensioni
) {}
