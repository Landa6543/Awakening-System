package com.soybe.awakeningsystem.casino.roulette;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class RouletteEngine {
    private static final SecureRandom RANDOM = new SecureRandom();

    public record SpinResult(
            int winningNumber,
            long totalBet,
            long totalPayout,
            List<RouletteBetEntry> winningBets
    ) {}

    public static SpinResult evaluate(List<RouletteBetEntry> bets) {
        int winningNumber = RANDOM.nextInt(RouletteWheel.POCKET_COUNT);
        return evaluateWithNumber(bets, winningNumber);
    }

    public static SpinResult evaluateWithNumber(List<RouletteBetEntry> bets, int winningNumber) {
        long totalBet = 0L;
        long totalPayout = 0L;
        List<RouletteBetEntry> winningBets = new ArrayList<>();

        for (RouletteBetEntry bet : bets) {
            totalBet += bet.amount();
            long payout = bet.calculatePayout(winningNumber);
            if (payout > 0) {
                totalPayout += payout;
                winningBets.add(bet);
            }
        }

        return new SpinResult(winningNumber, totalBet, totalPayout, winningBets);
    }
}
