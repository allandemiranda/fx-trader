package br.allandemiranda.fx.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
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
public class CandlestickDto implements Serializable {

    @NotNull
    @PastOrPresent
    LocalDateTime timestamp;

    @Positive
    double open;

    @Positive
    double high;

    @Positive
    double low;

    @Positive
    double close;

    @AssertTrue(message = "low must be <= open/close and high must be >= open/close")
    public boolean isPriceValid() {
        return this.getLow() <= this.getHigh() &&
                this.getOpen() <= this.getHigh() &&
                this.getOpen() >= this.getLow() &&
                this.getClose() <= this.getHigh() &&
                this.getClose() >= this.getLow();
    }
}