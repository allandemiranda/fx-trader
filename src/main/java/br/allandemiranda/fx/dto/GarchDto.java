package br.allandemiranda.fx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class GarchDto implements Serializable {

    @NotNull
    @PastOrPresent
    LocalDateTime timestamp;

    @PositiveOrZero
    double tpPips;

    @PositiveOrZero
    double slPips;

    @PositiveOrZero
    double sigmaPerBarPips;

    double mu;

    @PositiveOrZero
    double omega;

    @PositiveOrZero
    double alpha;

    @PositiveOrZero
    double beta;

    @PositiveOrZero
    double nu;

    @PositiveOrZero
    double breakEvenHitRate;

    boolean isLowPersistence;

    boolean isBetaInEdge;

    boolean isNuInEdge;

    boolean isInvertedQuantis;

    boolean isTpOmegaOut;

    boolean isSlOmegaSorter;

    boolean isSlOmegaLonger;

    boolean isOmegaPerHLonger;
}