package com.soybe.awakeningsystem.casino.slot;

public enum SlotSymbol {
    SLIME(0),
    MORA(5),
    STARDUST(8),
    STARGLITTER(15),
    PRIMOGEM(25),
    INTERTWINED_FATE(50),
    LIGHT_CONE(100),
    PAIMON(200),
    SEVEN(500);

    private final int tripleMultiplier;

    SlotSymbol(int tripleMultiplier) {
        this.tripleMultiplier = tripleMultiplier;
    }

    public int getTripleMultiplier() {
        return tripleMultiplier;
    }
}
