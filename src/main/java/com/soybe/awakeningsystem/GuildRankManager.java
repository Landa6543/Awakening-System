package com.soybe.awakeningsystem;

import java.util.List;

public final class GuildRankManager {
    private GuildRankManager() {}

    public static final List<String> RANKS = List.of("F", "E", "D", "C", "B", "A", "S");
    public static final int[] THRESHOLDS = {0, 10, 25, 50, 90, 140, 200};

    public static String getGuildRank(int completed) {
        String current = "F";
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (completed >= THRESHOLDS[i]) {
                current = RANKS.get(i);
            } else {
                break;
            }
        }
        return current;
    }

    public static int getNextRankNeeded(int completed) {
        for (int req : THRESHOLDS) {
            if (completed < req) {
                return req;
            }
        }
        return THRESHOLDS[THRESHOLDS.length - 1];
    }

    public static String getNextRankName(int completed) {
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (completed < THRESHOLDS[i]) {
                return RANKS.get(i);
            }
        }
        return "S (MAX)";
    }
}