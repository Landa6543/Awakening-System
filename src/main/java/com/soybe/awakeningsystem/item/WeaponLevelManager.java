package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/**
 * Gestor universal de subida de nivel de armas para Awakening System.
 * Permite que todas las armas del mod suban del nivel 1 al 100, escalen su daño,
 * otorguen bonificaciones de crítico y desbloqueen hitos en Lv. 10, 25, 50, 75 y 100.
 */
public final class WeaponLevelManager {
    public static final int MAX_WEAPON_LEVEL = 100;
    public static final String TAG_WEAPON_LEVEL = "AwakenedWeaponLevel";
    public static final String TAG_WEAPON_EXP = "AwakenedWeaponExp";
    public static final String TAG_TOTAL_KILLS = "AwakenedTotalKills";

    private WeaponLevelManager() {}

    /**
     * Determina si el objeto califica para subir de nivel de arma.
     */
    public static boolean isLevelableWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.getItem() instanceof AwakeningWeaponItem
                || stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof BowItem;
    }

    /**
     * Obtiene el nivel actual del arma (1 a 100).
     */
    public static int getLevel(ItemStack stack) {
        if (!isLevelableWeapon(stack)) return 1;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains(TAG_WEAPON_LEVEL)) {
            return Math.max(1, Math.min(MAX_WEAPON_LEVEL, cd.copyTag().getInt(TAG_WEAPON_LEVEL)));
        }
        return 1;
    }

    /**
     * Obtiene la experiencia acumulada en el nivel actual.
     */
    public static int getExp(ItemStack stack) {
        if (!isLevelableWeapon(stack)) return 0;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains(TAG_WEAPON_EXP)) {
            return Math.max(0, cd.copyTag().getInt(TAG_WEAPON_EXP));
        }
        return 0;
    }

    /**
     * Calcula la experiencia requerida para pasar del nivel actual al siguiente.
     * Fórmula: floor(100 * Level^1.35)
     */
    public static int getExpForNextLevel(int currentLevel) {
        if (currentLevel >= MAX_WEAPON_LEVEL) return 0;
        return Math.max(100, (int) Math.floor(100.0D * Math.pow(currentLevel, 1.35D)));
    }

    /**
     * Total de enemigos derrotados con esta arma específica.
     */
    public static int getTotalKills(ItemStack stack) {
        if (!isLevelableWeapon(stack)) return 0;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains(TAG_TOTAL_KILLS)) {
            return Math.max(0, cd.copyTag().getInt(TAG_TOTAL_KILLS));
        }
        return 0;
    }

    /**
     * Otorga experiencia al arma y gestiona la subida de nivel, efectos audiovisuales y notificaciones.
     * @return true si el arma subió al menos un nivel.
     */
    public static boolean addExp(ItemStack stack, int amount, ServerPlayer player) {
        if (!isLevelableWeapon(stack) || amount <= 0) return false;

        int currentLevel = getLevel(stack);
        if (currentLevel >= MAX_WEAPON_LEVEL) return false;

        int currentExp = getExp(stack);
        int kills = getTotalKills(stack);

        currentExp += amount;
        boolean leveledUp = false;
        int initialLevel = currentLevel;

        while (currentLevel < MAX_WEAPON_LEVEL) {
            int needed = getExpForNextLevel(currentLevel);
            if (currentExp >= needed) {
                currentExp -= needed;
                currentLevel++;
                leveledUp = true;
            } else {
                break;
            }
        }

        if (currentLevel >= MAX_WEAPON_LEVEL) {
            currentExp = 0; // Cap a nivel 100
        }

        // Guardar datos en el ItemStack
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = cd != null ? cd.copyTag() : new CompoundTag();
        tag.putInt(TAG_WEAPON_LEVEL, currentLevel);
        tag.putInt(TAG_WEAPON_EXP, currentExp);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Efectos y mensajes si subió de nivel
        if (leveledUp && player != null) {
            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 45, 0.5D, 0.6D, 0.5D, 0.15D);
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.5D, player.getZ(), 35, 0.4D, 0.5D, 0.4D, 0.2D);
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.1F);
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.9F, 1.3F);

            String weaponName = stack.getHoverName().getString();
            player.displayClientMessage(Component.literal("§6§l✦ ¡ARMA MEJORADA! §f" + weaponName + " §eha alcanzado el §6§lNivel " + currentLevel + "§e/100!"), false);

            // Anuncio si alcanza un hito legendario
            if (currentLevel == 10 || currentLevel == 25 || currentLevel == 50 || currentLevel == 75 || currentLevel == 100) {
                String milestoneTitle = switch (currentLevel) {
                    case 10 -> "§a[Hito I: Vínculo de Sangre]";
                    case 25 -> "§b[Hito II: Resonancia Marcial]";
                    case 50 -> "§d[Hito III: Despertar de Habilidad]";
                    case 75 -> "§6[Hito IV: Resplandor del Alma]";
                    case 100 -> "§e§l✦ [HITO SUPREMO: TRASCENDENCIA DIVINA] ✦";
                    default -> "";
                };
                player.displayClientMessage(Component.literal("§6§l✦ ¡NUEVO RASGO DESBLOQUEADO! " + milestoneTitle + " §7(Revisa las propiedades del arma)"), false);
                level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 2, 0.1D, 0.1D, 0.1D, 0.0D);
            }
        }

        return leveledUp;
    }

    /**
     * Registra una eliminación con el arma.
     */
    public static void recordKill(ItemStack stack) {
        if (!isLevelableWeapon(stack)) return;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = cd != null ? cd.copyTag() : new CompoundTag();
        int kills = tag.getInt(TAG_TOTAL_KILLS) + 1;
        tag.putInt(TAG_TOTAL_KILLS, kills);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Multiplicador de Daño de Ataque según el nivel del arma (+1.2% por nivel, hasta +118.8% a Lv. 100).
     */
    public static double getDamageMultiplier(ItemStack stack) {
        int lvl = getLevel(stack);
        return 1.0D + ((lvl - 1) * 0.012D);
    }

    /**
     * Probabilidad de Golpe Crítico adicional (+0.1% por nivel, hasta +10% a Lv. 100).
     */
    public static float getBonusCritChance(ItemStack stack) {
        int lvl = getLevel(stack);
        return (lvl - 1) * 0.001F;
    }

    /**
     * Penetración de Armadura (a partir de nivel 30, hasta 15%).
     */
    public static float getArmorPenetration(ItemStack stack) {
        int lvl = getLevel(stack);
        if (lvl < 30) return 0.0F;
        return Math.min(0.15F, (lvl - 30) * 0.002F);
    }

    /**
     * Comprueba si el arma cumple con un nivel de hito específico.
     */
    public static boolean hasMilestone(ItemStack stack, int milestone) {
        return getLevel(stack) >= milestone;
    }

    /**
     * Añade la información visual de nivel y barra de progreso a los tooltips del arma.
     */
    public static void appendWeaponLevelTooltip(ItemStack stack, List<Component> tooltip) {
        if (!isLevelableWeapon(stack)) return;

        int lvl = getLevel(stack);
        int exp = getExp(stack);
        int maxExp = getExpForNextLevel(lvl);
        int kills = getTotalKills(stack);

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§6§l✦ PROGRESO DEL ARMA ✦").withStyle(ChatFormatting.GOLD));

        if (lvl >= MAX_WEAPON_LEVEL) {
            tooltip.add(Component.literal("§e✦ Nivel: §6§lLv. 100 §e(TRASCENDENCIA MÁXIMA)").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal("§8[§6████████████████████§8] §a100%").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            float pct = maxExp > 0 ? (float) exp / (float) maxExp : 0.0F;
            int filled = Math.min(20, Math.max(0, (int) (pct * 20)));
            StringBuilder bar = new StringBuilder("§8[§a");
            for (int i = 0; i < filled; i++) bar.append("█");
            bar.append("§7");
            for (int i = filled; i < 20; i++) bar.append("░");
            bar.append("§8] §f").append(String.format("%.1f", pct * 100.0F)).append("%");

            tooltip.add(Component.literal("§e✦ Nivel: §fLv. " + lvl + "§8/§7100").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal(bar.toString()));
            tooltip.add(Component.literal("§7EXP: §f" + String.format("%,d", exp) + " §8/ §7" + String.format("%,d", maxExp)).withStyle(ChatFormatting.GRAY));
        }

        // Estadísticas escaladas
        double bonusDmgPct = (lvl - 1) * 1.2D;
        tooltip.add(Component.literal("§c⚔ Daño Adicional: §a+" + String.format("%.1f", bonusDmgPct) + "%").withStyle(ChatFormatting.RED));

        if (lvl > 1) {
            float crit = getBonusCritChance(stack) * 100.0F;
            tooltip.add(Component.literal("§6★ Probabilidad Crítica: §a+" + String.format("%.1f", crit) + "%").withStyle(ChatFormatting.GOLD));
        }

        if (lvl >= 30) {
            float pen = getArmorPenetration(stack) * 100.0F;
            tooltip.add(Component.literal("§b✦ Perforación de Armadura: §a+" + String.format("%.1f", pen) + "%").withStyle(ChatFormatting.AQUA));
        }

        if (kills > 0) {
            tooltip.add(Component.literal("§8☠ Bajas Totales: §7" + String.format("%,d", kills)).withStyle(ChatFormatting.DARK_GRAY));
        }

        // Hitos alcanzados
        tooltip.add(Component.literal("§8────────────────────────"));
        tooltip.add(Component.literal(lvl >= 10 ? "§a✔ Lv. 10: Vínculo de Sangre (-20% Coste Habilidades)" : "§8✖ Lv. 10: Vínculo de Sangre (Bloqueado)"));
        tooltip.add(Component.literal(lvl >= 25 ? "§b✔ Lv. 25: Resonancia Marcial (Onda de choque al crítico)" : "§8✖ Lv. 25: Resonancia Marcial (Bloqueado)"));
        tooltip.add(Component.literal(lvl >= 50 ? "§d✔ Lv. 50: Despertar de Habilidad (+Potencia y -Cooldown)" : "§8✖ Lv. 50: Despertar de Habilidad (Bloqueado)"));
        tooltip.add(Component.literal(lvl >= 75 ? "§6✔ Lv. 75: Resplandor del Alma (Aura 3D del Arma)" : "§8✖ Lv. 75: Resplandor del Alma (Bloqueado)"));
        tooltip.add(Component.literal(lvl >= 100 ? "§e✔ Lv. 100: Trascendencia Divina (Daño Final x1.5)" : "§8✖ Lv. 100: Trascendencia Divina (Bloqueado)"));
    }
}
