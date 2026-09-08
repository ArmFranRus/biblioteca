package com.biblioteca.dto;

import jakarta.validation.constraints.NotNull;

public record PrestitoRequest(
        @NotNull Long copiaId,
        @NotNull Long utenteId
) {}
