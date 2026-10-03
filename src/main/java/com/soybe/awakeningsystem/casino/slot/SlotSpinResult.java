package com.soybe.awakeningsystem.casino.slot;

import java.util.List;

public record SlotSpinResult(
        SlotSymbol[][] matrix,
        long totalWin,
        List<SlotLineResult> lines
) {}
