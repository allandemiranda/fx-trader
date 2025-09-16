package br.allandemiranda.fx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.time.LocalDateTime;

public record GarchDto(@NotNull LocalDateTime timestamp, @PositiveOrZero double lastSigmaPips, @PositiveOrZero double kTakeProfit, @PositiveOrZero double kStopLoss, @PositiveOrZero double var975,
                       @PositiveOrZero double var990) implements Serializable {
}