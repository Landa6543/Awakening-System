package com.soybe.awakeningsystem.combat;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Generates directional sweeping slash trail VFX, arc geometries, and elemental
 * particle themes for the Awakening Combat Engine (ACE).
 */
public class AceSlashVfx {

    public enum SlashTheme {
        FIRE_ABYSSAL(ParticleTypes.FLAME, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.LAVA),
        ICE_SOUL(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.END_ROD, ParticleTypes.ENCHANTED_HIT),
        LIGHTNING_THUNDER(ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FIREWORK, ParticleTypes.ENCHANTED_HIT),
        VOID_ASTRAL(ParticleTypes.PORTAL, ParticleTypes.DRAGON_BREATH, ParticleTypes.REVERSE_PORTAL),
        CELESTIAL_HOLY(ParticleTypes.TOTEM_OF_UNDYING, ParticleTypes.GLOW, ParticleTypes.END_ROD),
        JADE_EMERALD(ParticleTypes.HAPPY_VILLAGER, ParticleTypes.COMPOSTER, ParticleTypes.ENCHANTED_HIT),
        STEEL_WIND(ParticleTypes.SWEEP_ATTACK, ParticleTypes.CRIT, ParticleTypes.CLOUD),
        MARTIAL_KINETIC(ParticleTypes.CRIT, ParticleTypes.ENCHANTED_HIT, ParticleTypes.SWEEP_ATTACK);

        public final ParticleOptions primary;
        public final ParticleOptions secondary;
        public final ParticleOptions accent;

        SlashTheme(ParticleOptions primary, ParticleOptions secondary, ParticleOptions accent) {
            this.primary = primary;
            this.secondary = secondary;
            this.accent = accent;
        }
    }

    /**
     * Determines the elemental theme based on the weapon held or cultivator energy.
     */
    public static SlashTheme detectTheme(LivingEntity attacker, ItemStack weapon) {
        if (weapon.isEmpty()) {
            if (attacker instanceof Player player) {
                SystemData data = SystemEvents.data(player);
                if (data.cultivationQi() >= 2) {
                    return SlashTheme.CELESTIAL_HOLY;
                }
            }
            return SlashTheme.MARTIAL_KINETIC;
        }

        String itemName = weapon.getHoverName().getString().toLowerCase();
        String itemId = BuiltInRegistries.ITEM.getKey(weapon.getItem()).getPath().toLowerCase();
        String combined = itemId + " " + itemName;

        if (combined.contains("abyss") || combined.contains("flame") || combined.contains("fire") ||
                combined.contains("lava") || combined.contains("infernal") || combined.contains("blaze")) {
            return SlashTheme.FIRE_ABYSSAL;
        }
        if (combined.contains("soul") || combined.contains("frost") || combined.contains("ice") ||
                combined.contains("mithril") || combined.contains("winter") || combined.contains("chill") ||
                combined.contains("snow")) {
            return SlashTheme.ICE_SOUL;
        }
        if (combined.contains("thunder") || combined.contains("lightning") || combined.contains("spark") ||
                combined.contains("volt") || combined.contains("storm") || combined.contains("raiden")) {
            return SlashTheme.LIGHTNING_THUNDER;
        }
        if (combined.contains("astral") || combined.contains("void") || combined.contains("ender") ||
                combined.contains("shadow") || combined.contains("dark") || combined.contains("purpur")) {
            return SlashTheme.VOID_ASTRAL;
        }
        if (combined.contains("celestial") || combined.contains("holy") || combined.contains("sun") ||
                combined.contains("divine") || combined.contains("primordial") || combined.contains("gold") ||
                combined.contains("light")) {
            return SlashTheme.CELESTIAL_HOLY;
        }
        if (combined.contains("jade") || combined.contains("emerald") || combined.contains("bamboo") ||
                combined.contains("nature") || combined.contains("forest")) {
            return SlashTheme.JADE_EMERALD;
        }

        return SlashTheme.STEEL_WIND;
    }

    /**
     * Spawns client-side local slash particles with zero latency.
     */
    public static void spawnClientSlashTrail(Player player, int comboStep, ItemStack weapon) {
        Level level = player.level();
        SlashTheme theme = detectTheme(player, weapon);
        double range = Math.max(1.8, AceTargetFinder.getWeaponRange(player, weapon) * 0.82);

        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        if (right.lengthSqr() < 1e-4) {
            right = new Vec3(1, 0, 0);
        }
        Vec3 up = right.cross(look).normalize();

        int samplePoints = (comboStep == 2) ? 18 : 12;
        double startAngle, endAngle, startTilt, endTilt;

        switch (comboStep) {
            case 0 -> { // Diagonal descending: Top-Right to Bottom-Left
                startAngle = Math.toRadians(48.0);
                endAngle = Math.toRadians(-48.0);
                startTilt = 0.38;
                endTilt = -0.38;
            }
            case 1 -> { // Diagonal ascending: Bottom-Left to Top-Right
                startAngle = Math.toRadians(-48.0);
                endAngle = Math.toRadians(48.0);
                startTilt = -0.38;
                endTilt = 0.38;
            }
            default -> { // Finisher: Wide horizontal cleave / spin (150 degrees)
                startAngle = Math.toRadians(-75.0);
                endAngle = Math.toRadians(75.0);
                startTilt = 0.05;
                endTilt = -0.05;
            }
        }

        for (int i = 0; i <= samplePoints; i++) {
            double fraction = (double) i / (double) samplePoints;
            double angle = startAngle + (endAngle - startAngle) * fraction;
            double tilt = startTilt + (endTilt - startTilt) * fraction;

            Vec3 dir = look.scale(Math.cos(angle)).add(right.scale(Math.sin(angle))).normalize();
            Vec3 pos = eyePos.add(dir.scale(range)).add(up.scale(tilt));

            // Primary elemental trail
            level.addParticle(theme.primary, pos.x, pos.y, pos.z,
                    dir.x * 0.04, dir.y * 0.02, dir.z * 0.04);

            // Secondary particle on alternate points for dense aura
            if (i % 2 == 0) {
                level.addParticle(theme.secondary, pos.x, pos.y, pos.z,
                        0.0, 0.01, 0.0);
            }
        }

        // Finisher apex effect
        if (comboStep == 2) {
            Vec3 centerPos = eyePos.add(look.scale(range));
            level.addParticle(theme.accent, centerPos.x, centerPos.y, centerPos.z, 0, 0, 0);
            level.addParticle(ParticleTypes.SWEEP_ATTACK, centerPos.x, centerPos.y, centerPos.z, 0, 0, 0);
        }
    }

    /**
     * Broadcasts server-side slash particles to nearby players in multiplayer.
     */
    public static void broadcastServerSlashTrail(ServerPlayer player, int comboStep, ItemStack weapon) {
        ServerLevel level = player.serverLevel();
        SlashTheme theme = detectTheme(player, weapon);
        double range = Math.max(1.8, AceTargetFinder.getWeaponRange(player, weapon) * 0.82);

        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        if (right.lengthSqr() < 1e-4) {
            right = new Vec3(1, 0, 0);
        }
        Vec3 up = right.cross(look).normalize();

        int samplePoints = (comboStep == 2) ? 14 : 9;
        double startAngle, endAngle, startTilt, endTilt;

        switch (comboStep) {
            case 0 -> {
                startAngle = Math.toRadians(45.0);
                endAngle = Math.toRadians(-45.0);
                startTilt = 0.35;
                endTilt = -0.35;
            }
            case 1 -> {
                startAngle = Math.toRadians(-45.0);
                endAngle = Math.toRadians(45.0);
                startTilt = -0.35;
                endTilt = 0.35;
            }
            default -> {
                startAngle = Math.toRadians(-70.0);
                endAngle = Math.toRadians(70.0);
                startTilt = 0.05;
                endTilt = -0.05;
            }
        }

        for (int i = 0; i <= samplePoints; i++) {
            double fraction = (double) i / (double) samplePoints;
            double angle = startAngle + (endAngle - startAngle) * fraction;
            double tilt = startTilt + (endTilt - startTilt) * fraction;

            Vec3 dir = look.scale(Math.cos(angle)).add(right.scale(Math.sin(angle))).normalize();
            Vec3 pos = eyePos.add(dir.scale(range)).add(up.scale(tilt));

            level.sendParticles(theme.primary, pos.x, pos.y, pos.z, 1, 0.02, 0.02, 0.02, 0.02);
            if (i % 2 == 0) {
                level.sendParticles(theme.secondary, pos.x, pos.y, pos.z, 1, 0.01, 0.01, 0.01, 0.01);
            }
        }

        if (comboStep == 2) {
            Vec3 centerPos = eyePos.add(look.scale(range));
            level.sendParticles(theme.accent, centerPos.x, centerPos.y, centerPos.z, 8, 0.25, 0.25, 0.25, 0.08);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, centerPos.x, centerPos.y, centerPos.z, 1, 0, 0, 0, 0);
        }
    }
}
