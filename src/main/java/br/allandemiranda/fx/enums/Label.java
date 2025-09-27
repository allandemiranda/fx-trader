package br.allandemiranda.fx.enums;

public enum Label {
    BUY(0), SELL(1), NEUTRAL(2);
    public final int idx;

    Label(int i) {
        this.idx = i;
    }

    public static Label fromStrings(String buyTp, String sellTp, String label) {
        if (label != null && !label.isBlank()) return Label.valueOf(label.trim().toUpperCase());
        boolean b = buyTp != null && buyTp.trim().equals("1");
        boolean s = sellTp != null && sellTp.trim().equals("1");
        if (b && !s) return BUY;
        if (s && !b) return SELL;
        return NEUTRAL; // SL em ambos ou nenhum TP
    }
}
