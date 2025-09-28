package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.EMAsDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmasService {
    @NotNull
    EMAsDto add(@NotNull @Valid EMAsDto emasDto);

    @NotNull
    Optional<EMAsDto> getEmas(@Valid @NotNull @PastOrPresent LocalDateTime timestamp);
}
