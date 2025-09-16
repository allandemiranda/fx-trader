package br.allandemiranda.fx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CandlestickDto(@NotNull LocalDateTime openDateTime, @NotNull @Positive BigDecimal open, @NotNull @Positive BigDecimal high, @NotNull @Positive BigDecimal low, @NotNull @Positive BigDecimal close,
                             GarchDto garch) implements Serializable {
}