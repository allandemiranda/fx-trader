package br.allandemiranda.fx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum XGBLabel {
    NEUTRAL(0), BUY(1), SELL(2);

    public final int value;

}
