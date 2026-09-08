package com.biblioteca.dto;

import com.biblioteca.model.enums.Ruolo;

public record UtenteDTO(
        Long id,
        String email,
        String nome,
        String cognome,
        Ruolo ruolo,
        Boolean attivo
) {}
