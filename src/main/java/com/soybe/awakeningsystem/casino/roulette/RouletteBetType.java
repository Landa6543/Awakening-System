package com.soybe.awakeningsystem.casino.roulette;

public enum RouletteBetType {
    STRAIGHT_UP(35, "Pleno"),
    RED(1, "Rojo"),
    BLACK(1, "Negro"),
    EVEN(1, "Par"),
    ODD(1, "Impar"),
    LOW(1, "1 al 18 (Falta)"),
    HIGH(1, "19 al 36 (Pasa)"),
    DOZEN_1(2, "1ª Docena (1-12)"),
    DOZEN_2(2, "2ª Docena (13-24)"),
    DOZEN_3(2, "3ª Docena (25-36)"),
    COLUMN_1(2, "1ª Columna"),
    COLUMN_2(2, "2ª Columna"),
    COLUMN_3(2, "3ª Columna");

    private final int payoutMultiplier;
    private final String label;

    RouletteBetType(int payoutMultiplier, String label) {
        this.payoutMultiplier = payoutMultiplier;
        this.label = label;
    }

    public int getPayoutMultiplier() {
        return payoutMultiplier;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Evalúa si una apuesta resulta ganadora con respecto al número caído.
     * @param winningNumber número ganador (0 al 36)
     * @param param parámetro adicional (para STRAIGHT_UP es el número apostado 0..36; para otros es 0)
     */
    public boolean wins(int winningNumber, int param) {
        if (winningNumber == 0) {
            return this == STRAIGHT_UP && param == 0;
        }

        return switch (this) {
            case STRAIGHT_UP -> param == winningNumber;
            case RED -> RouletteWheel.getColor(winningNumber) == RouletteWheel.PocketColor.RED;
            case BLACK -> RouletteWheel.getColor(winningNumber) == RouletteWheel.PocketColor.BLACK;
            case EVEN -> winningNumber % 2 == 0;
            case ODD -> winningNumber % 2 != 0;
            case LOW -> winningNumber >= 1 && winningNumber <= 18;
            case HIGH -> winningNumber >= 19 && winningNumber <= 36;
            case DOZEN_1 -> winningNumber >= 1 && winningNumber <= 12;
            case DOZEN_2 -> winningNumber >= 13 && winningNumber <= 24;
            case DOZEN_3 -> winningNumber >= 25 && winningNumber <= 36;
            case COLUMN_1 -> winningNumber % 3 == 1;
            case COLUMN_2 -> winningNumber % 3 == 2;
            case COLUMN_3 -> winningNumber % 3 == 0;
        };
    }
}
