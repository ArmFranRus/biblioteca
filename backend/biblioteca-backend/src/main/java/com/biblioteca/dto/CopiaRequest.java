package com.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

public record CopiaRequest(@NotBlank String codiceInventario) {}
