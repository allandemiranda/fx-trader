package br.allandemiranda.fx.enums;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Timeframe {
    //@formatter:off
    M1("M1", 1, Frame.MINUTE),
    M5("M5", 5, Frame.MINUTE),
    M15("M15", 15, Frame.MINUTE),
    M30("M30", 30, Frame.MINUTE),
    H1("H1", 1, Frame.HOUR),
    H2("H2", 2, Frame.HOUR),
    H4("H4", 4, Frame.HOUR),
    H8("H8", 8, Frame.HOUR),
    D1("D1", 1, Frame.DAY);
    //@formatter:on

    @NotBlank
    @NotNull
    private final String name;
    @Positive
    private final int timeValue;
    @NotNull
    private final Frame frame;

    public enum Frame {
        MINUTE, HOUR, DAY
    }
}
