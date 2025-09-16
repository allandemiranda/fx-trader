package br.allandemiranda.fx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TickDto(@NotNull LocalDateTime dateTime, @NotNull @Positive BigDecimal bid, @NotNull @Positive BigDecimal ask) implements Serializable {
}