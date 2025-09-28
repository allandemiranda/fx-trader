package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.RSIDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RsiService {
    @NotNull
    RSIDto addRSI(@NotNull @Valid RSIDto rsiDto);

    Optional<RSIDto> getRSI(@NotNull @Valid @PastOrPresent LocalDateTime timestamp);
}
