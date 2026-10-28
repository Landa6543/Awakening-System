package com.soybe.awakeningsystem.casino.slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SlotReels {
    public static final int REEL_SIZE = 256;

    public static final SlotSymbol[] REEL1;
    public static final SlotSymbol[] REEL2;
    public static final SlotSymbol[] REEL3;
    public static final SlotSymbol[][] STRIPS;

    static {
        REEL1 = generateReel(0x47414348414CL);
        REEL2 = generateReel(0x47414348414CL ^ 0x5555L);
        REEL3 = generateReel(0x47414348414CL ^ 0xAAAAL);
        STRIPS = new SlotSymbol[][] { REEL1, REEL2, REEL3 };
    }

    public static SlotSymbol get(SlotSymbol[] reel, int index) {
        int i = Math.floorMod(index, REEL_SIZE);
        return reel[i];
    }

    private static SlotSymbol[] generateReel(long seed) {
        List<SlotSymbol> symbols = new ArrayList<>(REEL_SIZE);

        addSymbols(symbols, SlotSymbol.SLIME, 85);
        addSymbols(symbols, SlotSymbol.MORA, 55);
        addSymbols(symbols, SlotSymbol.STARDUST, 40);
        addSymbols(symbols, SlotSymbol.STARGLITTER, 30);
        addSymbols(symbols, SlotSymbol.PRIMOGEM, 20);
        addSymbols(symbols, SlotSymbol.INTERTWINED_FATE, 14);
        addSymbols(symbols, SlotSymbol.LIGHT_CONE, 11);
        addSymbols(symbols, SlotSymbol.SEVEN, 1);

        while (symbols.size() < REEL_SIZE) {
            symbols.add(SlotSymbol.SLIME);
        }

        Random r = new Random(seed);
        for (int i = 0; i < 5; i++) {
            Collections.shuffle(symbols, r);
        }

        return symbols.toArray(new SlotSymbol[0]);
    }

    private static void addSymbols(List<SlotSymbol> list, SlotSymbol sym, int count) {
        for (int i = 0; i < count; i++) {
            list.add(sym);
        }
    }
}
