package com.soybe.awakeningsystem.seasonal;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.config.BossCompatibilityManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDate;
import java.time.Month;

/**
 * Seasonal Holiday Events Manager (Section 42 of Roadmap).
 * Controls automated seasonal events according to real-world calendar:
 * - Halloween: October 15th to November 5th.
 * - Christmas: December 1st to January 7th.
 * Includes creator/admin bypass via `/system dev season <halloween|christmas|off>`.
 */
public final class SeasonalEventManager {
    public enum Season {
        OFF,
        HALLOWEEN,
        CHRISTMAS;

        public static Season fromString(String name) {
            if (name == null) return OFF;
            return switch (name.toLowerCase(java.util.Locale.ROOT)) {
                case "halloween", "spooky" -> HALLOWEEN;
                case "christmas", "xmas", "navidad" -> CHRISTMAS;
                default -> OFF;
            };
        }
    }

    private static Season devOverride = null;

    private SeasonalEventManager() {}

    public static void setDevOverride(Season season) {
        devOverride = season;
    }

    public static Season getDevOverride() {
        return devOverride;
    }

    public static Season getCurrentSeason() {
        if (devOverride != null) {
            return devOverride;
        }

        LocalDate now = LocalDate.now();
        Month month = now.getMonth();
        int day = now.getDayOfMonth();

        // Halloween: October 15 to November 5
        if ((month == Month.OCTOBER && day >= 15) || (month == Month.NOVEMBER && day <= 5)) {
            return Season.HALLOWEEN;
        }

        // Christmas: December 1 to January 7
        if (month == Month.DECEMBER || (month == Month.JANUARY && day <= 7)) {
            return Season.CHRISTMAS;
        }

        return Season.OFF;
    }

    public static boolean isHalloweenActive() {
        return getCurrentSeason() == Season.HALLOWEEN;
    }

    public static boolean isChristmasActive() {
        return getCurrentSeason() == Season.CHRISTMAS;
    }

    public static boolean isAnySeasonActive() {
        return getCurrentSeason() != Season.OFF;
    }

    /**
     * Called on enemy monster defeat to roll seasonal drops.
     */
    public static void handleMobDrop(LivingEntity victim, ServerPlayer killer) {
        if (victim == null || killer == null || !(victim.level() instanceof ServerLevel level)) {
            return;
        }

        Season season = getCurrentSeason();
        if (season == Season.OFF) return;

        // Only hostiles or enemies drop seasonal goodies
        boolean isEnemy = victim instanceof Enemy || !victim.getType().getCategory().isFriendly();
        if (!isEnemy) return;

        boolean isBoss = BossCompatibilityManager.isBoss(victim);
        boolean isTowerChampion = victim.getPersistentData().getBoolean("TowerChampion")
                || victim.getPersistentData().getBoolean("TowerFinalBoss");

        float dropChance = (isBoss || isTowerChampion) ? 1.0F : 0.15F;

        if (level.random.nextFloat() < dropChance) {
            ItemStack dropStack = ItemStack.EMPTY;

            if (season == Season.CHRISTMAS) {
                if (SystemItems.CHRISTMAS_GIFT_BOX != null && SystemItems.CHRISTMAS_GIFT_BOX.isBound()) {
                    dropStack = new ItemStack(SystemItems.CHRISTMAS_GIFT_BOX.value(), (isBoss ? 2 : 1));
                    level.sendParticles(ParticleTypes.SNOWFLAKE, victim.getX(), victim.getY() + 0.5D, victim.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
                    level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.2F);
                }
            } else if (season == Season.HALLOWEEN) {
                if (SystemItems.CANDY_BASKET != null && SystemItems.CANDY_BASKET.isBound()) {
                    dropStack = new ItemStack(SystemItems.CANDY_BASKET.value(), (isBoss ? 2 : 1));
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, victim.getX(), victim.getY() + 0.5D, victim.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
                    level.sendParticles(ParticleTypes.SMOKE, victim.getX(), victim.getY() + 0.5D, victim.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.02D);
                    level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SOUL_SAND_BREAK, SoundSource.PLAYERS, 0.9F, 0.8F);
                }
            }

            if (!dropStack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level, victim.getX(), victim.getY() + 0.5D, victim.getZ(), dropStack);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
    }
}
