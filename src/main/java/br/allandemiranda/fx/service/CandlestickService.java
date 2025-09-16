package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.CandlestickDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface CandlestickService {
    @SneakyThrows
    void consumerCandlesticks(@Valid @NotNull Path candlestickFile, @Valid @NotNull Consumer<CandlestickDto> consumer);
}
