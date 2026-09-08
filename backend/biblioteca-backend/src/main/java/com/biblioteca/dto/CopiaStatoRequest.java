package com.biblioteca.dto;

import com.biblioteca.model.enums.StatoCopia;
import jakarta.validation.constraints.NotNull;

public record CopiaStatoRequest(@NotNull StatoCopia stato) {}
