package com.soybe.awakeningsystem.entity.hypostasis;

public enum HypostasisAttackType {
    NONE(0, 0),
    CLAP(1, 70),
    MISSILES(2, 90),
    SPECIAL_ACTION(3, 110),
    BURST_ULTIMATE(4, 120);

    private final int id;
    private final int durationTicks;

    HypostasisAttackType(int id, int durationTicks) {
        this.id = id;
        this.durationTicks = durationTicks;
    }

    public int getId() {
        return id;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public static HypostasisAttackType byId(int id) {
        for (HypostasisAttackType t : values()) {
            if (t.id == id) return t;
        }
        return NONE;
    }
}
