package com.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

public record AutoreRequest(@NotBlank String nome) {}
