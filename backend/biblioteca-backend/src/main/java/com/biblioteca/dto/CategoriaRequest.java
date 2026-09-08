package com.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(@NotBlank String nome) {}
