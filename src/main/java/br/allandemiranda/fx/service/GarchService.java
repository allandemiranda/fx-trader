package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.GarchDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GarchService {
    @NotNull
    GarchDto addGarch(@NotNull @Valid GarchDto garchDto);

    @NotNull
    Optional<GarchDto> getGarch(@Valid @NotNull @PastOrPresent LocalDateTime timestamp);
}
