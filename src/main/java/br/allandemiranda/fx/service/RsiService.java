package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.RSIDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface RsiService {
    @NotNull
    RSIDto addRSI(@NotNull @Valid RSIDto rsiDto);
}
