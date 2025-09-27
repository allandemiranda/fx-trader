package br.allandemiranda.fx.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record MLDto(@PositiveOrZero float buyProb, @PositiveOrZero float sellProb, @PositiveOrZero float neutralProb) {
}
