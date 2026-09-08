package com.biblioteca.dto;

import com.biblioteca.model.enums.StatoCopia;

public record CopiaDTO(Long id, Long libroId, String codiceInventario, StatoCopia stato) {}
