package com.soybe.awakeningsystem;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SystemConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue COIN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue EXP_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue BOSS_COIN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue PRESTIGE_COIN_BONUS_PERCENT;
    public static final ModConfigSpec.IntValue ENERGY_REGEN_TICKS;
    public static final ModConfigSpec.IntValue ENERGY_REGEN_AMOUNT;
    public static final ModConfigSpec.BooleanValue EVENT_NIGHT_BONUS;
    public static final ModConfigSpec.BooleanValue DAILY_MISSIONS_ENABLED;
    public static final ModConfigSpec.BooleanValue WEEKLY_MISSIONS_ENABLED;
    public static final ModConfigSpec.BooleanValue ACTIVE_SKILLS_ENABLED;
    public static final ModConfigSpec.IntValue DAILY_LOGIN_BONUS_COINS;
    public static final ModConfigSpec.IntValue DAILY_LOGIN_BONUS_EXP;

    // --- ESCALADO DE CRIATURAS POR DISTANCIA ---
    public static final ModConfigSpec.BooleanValue MOB_SCALING_ENABLED;
    public static final ModConfigSpec.IntValue MOB_SCALING_BLOCKS_PER_TIER;
    public static final ModConfigSpec.DoubleValue MOB_SCALING_HEALTH_PER_TIER;
    public static final ModConfigSpec.DoubleValue MOB_SCALING_DAMAGE_PER_TIER;
    public static final ModConfigSpec.DoubleValue MOB_SCALING_ARMOR_PER_TIER;
    public static final ModConfigSpec.IntValue MOB_SCALING_MAX_TIER;
    public static final ModConfigSpec.DoubleValue MOB_SCALING_EXP_BONUS_PER_TIER;
    public static final ModConfigSpec.BooleanValue MOB_SCALING_SHOW_NAME_TAG;
    public static final ModConfigSpec.BooleanValue MOB_SCALING_EXCLUDE_BOSSES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Awakening System Server Settings").push("general");

        COIN_MULTIPLIER = builder.comment("Multiplier for mob kill coins")
                .defineInRange("coin_multiplier", 1.0D, 0.1D, 10.0D);

        EXP_MULTIPLIER = builder.comment("Multiplier for affinity and combat experience")
                .defineInRange("exp_multiplier", 1.0D, 0.1D, 10.0D);

        BOSS_COIN_MULTIPLIER = builder.comment("Multiplier for boss kill coins")
                .defineInRange("boss_coin_multiplier", 3.0D, 1.0D, 20.0D);

        PRESTIGE_COIN_BONUS_PERCENT = builder.comment("Bonus coin multiplier per prestige level (0.15 = +15% per level)")
                .defineInRange("prestige_coin_bonus_percent", 0.15D, 0.0D, 1.0D);

        ENERGY_REGEN_TICKS = builder.comment("Ticks between passive energy regeneration (20 ticks = 1s)")
                .defineInRange("energy_regen_ticks", 40, 10, 200);

        ENERGY_REGEN_AMOUNT = builder.comment("Energy recovered per regeneration cycle")
                .defineInRange("energy_regen_amount", 5, 1, 50);

        EVENT_NIGHT_BONUS = builder.comment("Enable Awakening Hour: bonus coin & EXP at night")
                .define("event_night_bonus", true);

        DAILY_MISSIONS_ENABLED = builder.comment("Enable daily missions")
                .define("daily_missions_enabled", true);

        WEEKLY_MISSIONS_ENABLED = builder.comment("Enable weekly missions")
                .define("weekly_missions_enabled", true);

        ACTIVE_SKILLS_ENABLED = builder.comment("Enable active skill usage")
                .define("active_skills_enabled", true);

        DAILY_LOGIN_BONUS_COINS = builder.comment("Coins awarded upon first daily login")
                .defineInRange("daily_login_bonus_coins", 25, 0, 500);

        DAILY_LOGIN_BONUS_EXP = builder.comment("EXP awarded upon first daily login")
                .defineInRange("daily_login_bonus_exp", 50, 0, 1000);

        builder.pop();

        builder.comment("Distance-Based Mob Scaling Settings").push("mob_scaling");

        MOB_SCALING_ENABLED = builder.comment("Enable distance-based mob scaling (monsters get stronger the further you explore from spawn)")
                .define("enabled", true);

        MOB_SCALING_BLOCKS_PER_TIER = builder.comment("Distance in blocks per scaling level (e.g. 500 = level 1 at 500 blocks, level 2 at 1000 blocks)")
                .defineInRange("blocks_per_tier", 500, 50, 10000);

        MOB_SCALING_HEALTH_PER_TIER = builder.comment("Max Health multiplier bonus per tier (0.12 = +12% max health per tier)")
                .defineInRange("health_bonus_per_tier", 0.12D, 0.0D, 5.0D);

        MOB_SCALING_DAMAGE_PER_TIER = builder.comment("Attack Damage multiplier bonus per tier (0.08 = +8% attack damage per tier)")
                .defineInRange("damage_bonus_per_tier", 0.08D, 0.0D, 5.0D);

        MOB_SCALING_ARMOR_PER_TIER = builder.comment("Flat Armor bonus per tier (+0.5 armor per tier)")
                .defineInRange("armor_bonus_per_tier", 0.5D, 0.0D, 10.0D);

        MOB_SCALING_MAX_TIER = builder.comment("Maximum scaling tier cap")
                .defineInRange("max_tier", 50, 1, 500);

        MOB_SCALING_EXP_BONUS_PER_TIER = builder.comment("Bonus EXP dropped per tier (0.10 = +10% EXP per tier)")
                .defineInRange("exp_bonus_per_tier", 0.10D, 0.0D, 5.0D);

        MOB_SCALING_SHOW_NAME_TAG = builder.comment("Show level indicator [Nv. X] when looking at scaled mobs")
                .define("show_level_name_tag", true);

        MOB_SCALING_EXCLUDE_BOSSES = builder.comment("Exclude custom bosses from generic distance scaling")
                .define("exclude_bosses", true);

        builder.pop();

        SPEC = builder.build();
    }

    private SystemConfig() {}
}
