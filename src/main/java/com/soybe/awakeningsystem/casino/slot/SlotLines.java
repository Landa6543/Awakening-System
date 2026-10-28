package com.soybe.awakeningsystem.casino.slot;

import java.util.ArrayList;
import java.util.List;

public class SlotLines {

    public static final int[][] MODE1 = {
            {1, 0}, {1, 1}, {1, 2}
    };

    public static final int[][][] MODE2 = {
            { {0, 0}, {0, 1}, {0, 2} },
            { {1, 0}, {1, 1}, {1, 2} },
            { {2, 0}, {2, 1}, {2, 2} }
    };

    public static final int[][][] MODE3 = {
            { {0, 0}, {0, 1}, {0, 2} },
            { {1, 0}, {1, 1}, {1, 2} },
            { {2, 0}, {2, 1}, {2, 2} },
            { {0, 0}, {1, 1}, {2, 2} },
            { {2, 0}, {1, 1}, {0, 2} }
    };

    public static int[][][] getLines(int mode) {
        return switch (mode) {
            case 2 -> MODE2;
            case 3 -> MODE3;
            default -> new int[][][]{ MODE1 };
        };
    }

    public static int getLineCount(int mode) {
        return switch (mode) {
            case 2 -> 3;
            case 3 -> 5;
            default -> 1;
        };
    }

    private static SlotLineResult evaluateLine(SlotSymbol[][] matrix, int[][] coords, int baseBet, int lineIndex) {
        SlotSymbol a = matrix[ coords[0][0] ][ coords[0][1] ];
        SlotSymbol b = matrix[ coords[1][0] ][ coords[1][1] ];
        SlotSymbol c = matrix[ coords[2][0] ][ coords[2][1] ];

        SlotSymbol target = a;
        boolean allSame = (a == b && b == c);

        int multiplier;
        int symbolCount;

        if (target == SlotSymbol.MORA) {
            if (a == SlotSymbol.MORA && b != SlotSymbol.MORA) {
                multiplier = 1;
                symbolCount = 1;
            } else if (a == SlotSymbol.MORA && b == SlotSymbol.MORA && c != SlotSymbol.MORA) {
                multiplier = 2;
                symbolCount = 2;
            } else if (a == SlotSymbol.MORA && b == SlotSymbol.MORA && c == SlotSymbol.MORA) {
                multiplier = 5;
                symbolCount = 3;
            } else {
                multiplier = 0;
                symbolCount = 0;
            }
            return new SlotLineResult(multiplier > 0, SlotSymbol.MORA, symbolCount,
                    multiplier, (long) baseBet * multiplier, lineIndex);
        }

        if (target == SlotSymbol.SLIME) {
            return new SlotLineResult(false, null, 0, 0, 0, lineIndex);
        }

        if (allSame) {
            multiplier = target.getTripleMultiplier();
            return new SlotLineResult(true, target, 3, multiplier, (long) baseBet * multiplier, lineIndex);
        }

        return new SlotLineResult(false, null, 0, 0, 0, lineIndex);
    }

    public static SlotSpinResult evaluateSpin(SlotSymbol[][] matrix, int baseBet, int mode) {
        int[][][] lines = getLines(mode);
        List<SlotLineResult> results = new ArrayList<>();
        long total = 0;

        for (int i = 0; i < lines.length; i++) {
            int[][] line = lines[i];
            SlotLineResult result = evaluateLine(matrix, line, baseBet, i);
            results.add(result);
            total += result.lineWin();
        }

        return new SlotSpinResult(matrix, total, results);
    }
}
