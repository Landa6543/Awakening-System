package com.soybe.awakeningsystem.entity.hypostasis;

public enum HypostasisAttackType {
    NONE(0, 0),
    CLAP(1, 70),          // 3.5 segundos: Carga, split, golpe y ondas
    MISSILES(2, 90),      // 4.5 segundos: Cubos en círculo disparando
    SPECIAL_ACTION(3, 110), // Anemo: Tornados / Electro: Taladro
    BURST_ULTIMATE(4, 120); // Anemo: Mariposa / Electro: Láseres giratorios

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
