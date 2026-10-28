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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import com.soybe.awakeningsystem.item.pouch.PouchItem;

import java.util.List;

/**
 * Gestor universal de subida de nivel de armas para Awakening System.
 * Permite que todas las armas del mod, uchigatanas, guantes, espadas y armas moddeadas suban del nivel 1 al 100, escalen su daño,
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
     * Compatible con armas de Awakening System, espadas vanilla, arcos, hachas, uchigatanas, guantes/puños y armas moddeadas con daño de ataque.
     */
    public static boolean isLevelableWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        // Las armas con niveles e hitos individuales NUNCA son apilables (evita cuencos, lingotes, materiales, etc.)
        if (stack.getMaxStackSize() > 1) {
            return false;
        }

        Item item = stack.getItem();

        // 1. Exclusiones explícitas inmediatas: Bolsas, mochilas, armaduras, consumibles, escudos, herramientas utilitarias, bloques, etc.
        if (item instanceof PouchItem
                || item instanceof MoraPouchItem
                || item instanceof OmnisciencePouchItem
                || item instanceof BundleItem
                || item instanceof ArmorItem
                || item instanceof BlockItem
                || item instanceof ShieldItem
                || item instanceof net.minecraft.world.item.BucketItem
                || item instanceof net.minecraft.world.item.ShearsItem
                || item instanceof net.minecraft.world.item.FishingRodItem
                || item instanceof net.minecraft.world.item.FlintAndSteelItem
                || item instanceof net.minecraft.world.item.PotionItem
                || item instanceof net.minecraft.world.item.SaddleItem
                || item instanceof net.minecraft.world.item.NameTagItem
                || item instanceof net.minecraft.world.item.LeadItem
                || stack.has(DataComponents.FOOD)) {
            return false;
        }

        String path = BuiltInRegistries.ITEM.getKey(item).getPath().toLowerCase(java.util.Locale.ROOT);
        // Exclusión por nombre de contenedor, bolsa, amuleto, libro, armadura o consumible
        if (path.contains("pouch") || path.contains("bag") || path.contains("backpack") || path.contains("bundle")
                || path.contains("wallet") || path.contains("chestplate") || path.contains("helmet") || path.contains("leggings")
                || path.contains("boots") || path.contains("ring") || path.contains("necklace") || path.contains("amulet")
                || path.contains("potion") || path.contains("scroll") || path.contains("tome") || path.contains("book")
                || path.contains("relic") || path.contains("artifact") || path.contains("token") || path.contains("core")
                || path.contains("bowl") || path.contains("coin") || path.contains("ingot") || path.contains("ore")) {
            return false;
        }

        // 2. Armas reconocidas de Awakening System, espadas, arcos, herramientas de minería/tala
        if (item instanceof AwakeningWeaponItem
                || item instanceof SwordItem
                || item instanceof BowItem
                || item instanceof DiggerItem) {
            return true;
        }

        if (stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)) {
            return true;
        }

        // 3. Palabras clave precisas de armas (evitar falsos positivos como wand en wanderer, fan en fang, bow en bowl)
        if (path.contains("katana") || path.contains("uchigatana") || path.contains("sword") || path.contains("blade")
                || path.contains("glove") || path.contains("guante") || path.contains("fist") || path.contains("claw")
                || path.contains("dagger") || path.contains("spear") || path.contains("scythe") || path.contains("mace")
                || path.contains("hammer") || path.contains("rapier") || path.contains("sabre") || path.contains("tachi")
                || path.contains("nodachi") || path.contains("wakizashi") || path.contains("greatsword") || path.contains("claymore")
                || (path.endsWith("bow") || path.contains("bow_") || path.contains("_bow")) || path.contains("crossbow")
                || path.contains("staff")
                || (path.endsWith("wand") || path.contains("wand_") || path.contains("_wand"))
                || path.contains("polearm") || path.contains("whip")
                || (path.endsWith("fan") || path.contains("fan_") || path.contains("_fan"))
                || path.contains("cestus") || path.contains("gauntlet") || path.contains("knuckle")) {
            return true;
        }

        ItemAttributeModifiers attrComponent = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attrComponent != null) {
            for (var entry : attrComponent.modifiers()) {
                if (entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.modifier().amount() > 0.0D) {
                    return true;
                }
            }
        }

        return false;
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
            currentExp = 0;
        }

        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = cd != null ? cd.copyTag() : new CompoundTag();
        tag.putInt(TAG_WEAPON_LEVEL, currentLevel);
        tag.putInt(TAG_WEAPON_EXP, currentExp);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (leveledUp && player != null) {
            ServerLevel level = player.serverLevel();
            String weaponName = stack.getHoverName().getString();

            if (currentLevel == 10 || currentLevel == 25 || currentLevel == 50 || currentLevel == 75 || currentLevel == 100) {
                Component milestoneTitle = Component.translatable("awakening_system.weapon_level.milestone." + currentLevel);

                level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 2, 0.1D, 0.1D, 0.1D, 0.0D);
                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 50, 0.6D, 0.7D, 0.6D, 0.15D);

                player.playNotifySound(SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.85F, 1.6F);
                player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.35F);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.55F, 1.7F);

                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(8, 45, 15));
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(milestoneTitle));
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                        Component.translatable("awakening_system.weapon_level.notify.subtitle", weaponName, currentLevel)
                ));

                player.displayClientMessage(Component.translatable("awakening_system.weapon_level.notify.actionbar", milestoneTitle, weaponName, currentLevel), true);
                player.displayClientMessage(Component.translatable("awakening_system.weapon_level.notify.chat", weaponName, milestoneTitle, currentLevel), false);
            } else {
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.1D, player.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.15D);
                player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.75F, 1.45F);
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.85F);

                player.displayClientMessage(Component.translatable("awakening_system.weapon_level.notify.level_up", weaponName, currentLevel), true);
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
        tooltip.add(Component.translatable("awakening_system.weapon_level.header").withStyle(ChatFormatting.GOLD));

        if (lvl >= MAX_WEAPON_LEVEL) {
            tooltip.add(Component.translatable("awakening_system.weapon_level.max").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal("§8[§6████████████████████§8] §a100%").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            float pct = maxExp > 0 ? (float) exp / (float) maxExp : 0.0F;
            int filled = Math.min(20, Math.max(0, (int) (pct * 20)));
            StringBuilder bar = new StringBuilder("§8[§a");
            for (int i = 0; i < filled; i++) bar.append("█");
            bar.append("§7");
            for (int i = filled; i < 20; i++) bar.append("░");
            bar.append("§8] §f").append(String.format("%.1f", pct * 100.0F)).append("%");

            tooltip.add(Component.translatable("awakening_system.weapon_level.level", lvl).withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal(bar.toString()));
            tooltip.add(Component.translatable("awakening_system.weapon_level.exp", String.format("%,d", exp), String.format("%,d", maxExp)).withStyle(ChatFormatting.GRAY));
        }

        double bonusDmgPct = (lvl - 1) * 1.2D;
        tooltip.add(Component.translatable("awakening_system.weapon_level.bonus_dmg", String.format("%.1f", bonusDmgPct)).withStyle(ChatFormatting.RED));

        if (lvl > 1) {
            float crit = getBonusCritChance(stack) * 100.0F;
            tooltip.add(Component.translatable("awakening_system.weapon_level.crit_chance", String.format("%.1f", crit)).withStyle(ChatFormatting.GOLD));
        }

        if (lvl >= 30) {
            float pen = getArmorPenetration(stack) * 100.0F;
            tooltip.add(Component.translatable("awakening_system.weapon_level.armor_pen", String.format("%.1f", pen)).withStyle(ChatFormatting.AQUA));
        }

        if (kills > 0) {
            tooltip.add(Component.translatable("awakening_system.weapon_level.total_kills", String.format("%,d", kills)).withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.literal("§8────────────────────────"));
        tooltip.add(Component.translatable(lvl >= 10 ? "awakening_system.weapon_level.m10.unlocked" : "awakening_system.weapon_level.m10.locked"));
        tooltip.add(Component.translatable(lvl >= 25 ? "awakening_system.weapon_level.m25.unlocked" : "awakening_system.weapon_level.m25.locked"));
        tooltip.add(Component.translatable(lvl >= 50 ? "awakening_system.weapon_level.m50.unlocked" : "awakening_system.weapon_level.m50.locked"));
        tooltip.add(Component.translatable(lvl >= 75 ? "awakening_system.weapon_level.m75.unlocked" : "awakening_system.weapon_level.m75.locked"));
        tooltip.add(Component.translatable(lvl >= 100 ? "awakening_system.weapon_level.m100.unlocked" : "awakening_system.weapon_level.m100.locked"));
    }
}
