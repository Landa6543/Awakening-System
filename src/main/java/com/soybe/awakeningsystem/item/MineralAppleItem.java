package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class MineralAppleItem extends Item {

    public enum AppleTier {
        COPPER(1, "copper", "Cobre", 5, 0xFFD97040),
        IRON(2, "iron", "Hierro", 12, 0xFFE0E5E8),
        DIAMOND(4, "diamond", "Diamante", 32, 0xFF55FFFF),
        ABYSSAL(5, "abyssal", "Abisal", 45, 0xFFB04EE6),
        NETHERITE(6, "netherite", "Netherite", 55, 0xFF5E5354),
        SOUL_ALLOY(7, "soul_alloy", "Aleación de Almas", 68, 0xFF3CD8C6),
        ASTRALITE(8, "astralite", "Astralita", 80, 0xFF9955FF),
        MITHRIL(9, "mithril", "Mithril", 92, 0xFF8EECF5),
        ADAMANTITE(10, "adamantite", "Adamantita", 105, 0xFFE62E52),
        SPIRIT_JADE(11, "spirit_jade", "Jade Espiritual", 118, 0xFF00A86B),
        ASTRAL(12, "astral", "Metal Astral", 128, 0xFF88B0F8),
        CELESTIAL(13, "celestial", "Celestial", 140, 0xFFFFD700),
        PRIMORDIAL(14, "primordial", "Primordial", 155, 0xFF17C3B2);

        public final int tierNumber;
        public final String idName;
        public final String displayName;
        public final int reqLevel;
        public final int color;

        AppleTier(int tierNumber, String idName, String displayName, int reqLevel, int color) {
            this.tierNumber = tierNumber;
            this.idName = idName;
            this.displayName = displayName;
            this.reqLevel = reqLevel;
            this.color = color;
        }
    }

    private final AppleTier tier;
    private final boolean enchanted;

    public MineralAppleItem(AppleTier tier, boolean enchanted) {
        super(new Item.Properties()
                .food(createFoodProperties(tier, enchanted))
                .rarity(enchanted ? Rarity.EPIC : (tier.tierNumber >= 8 ? Rarity.EPIC : (tier.tierNumber >= 4 ? Rarity.RARE : Rarity.UNCOMMON)))
                .stacksTo(64));
        this.tier = tier;
        this.enchanted = enchanted;
    }

    private static FoodProperties createFoodProperties(AppleTier tier, boolean enchanted) {
        return new FoodProperties.Builder()
                .nutrition(enchanted ? 8 : 5)
                .saturationModifier(enchanted ? 1.4F : 1.0F)
                .alwaysEdible()
                .build();
    }

    public AppleTier getTier() {
        return tier;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return enchanted || super.isFoil(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            applyAppleEffects(player);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    private void applyAppleEffects(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);

        switch (tier) {
            case COPPER -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 4800, 1)); // 4 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 4800, 1)); // Haste II
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 4800, 1)); // Speed II
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4800, 0)); // Res I
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4800, 0));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0)); // 2 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2400, 0)); // Haste I
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 0)); // Speed I
                }
            }
            case IRON -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 4800, 2)); // 6 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4800, 1)); // Res II
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 4800, 0)); // Str I
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 1)); // 4 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2400, 0)); // Res I
                }
            }
            case DIAMOND -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 4800, 4)); // 10 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 3)); // Regen IV
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4800, 2)); // Res III
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 1)); // Str II
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 2)); // 6 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2)); // Regen III
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2400, 1)); // Res II
                }
            }
            case ABYSSAL -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 5)); // 12 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1800, 4)); // Regen V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 2)); // Str III
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 6000, 0));
                    if (data != null) {
                        data.restoreMana(200);
                        AwakeningSystemMod.sync(player);
                    }
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 3)); // 8 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 3)); // Regen IV
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 1)); // Str II
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 3600, 0));
                }
            }
            case NETHERITE -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 6)); // 14 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 3)); // Str IV
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 3)); // Res IV
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1800, 3)); // Regen IV
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 4)); // 10 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 2)); // Str III
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2400, 2)); // Res III
                }
            }
            case SOUL_ALLOY -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 7)); // 16 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3600, 4)); // Regen V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4800, 3)); // Res IV
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 3)); // Str IV
                    if (data != null) {
                        data.restoreMana(150);
                        AwakeningSystemMod.sync(player);
                    }
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 5)); // 12 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1800, 4)); // Regen V
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, 1)); // Speed II
                }
            }
            case ASTRALITE -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 8)); // 18 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2400, 4)); // Regen V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 3)); // Res IV
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 4800, 3)); // Str IV
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 4800, 0));
                    if (data != null) {
                        data.restoreMana(400);
                        AwakeningSystemMod.sync(player);
                    }
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 4800, 6)); // 14 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 4800, 2)); // Jump III
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 4800, 2)); // Speed III
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 4800, 0));
                }
            }
            case MITHRIL -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 9)); // 20 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3000, 5)); // Regen VI
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 4)); // Res V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 4800, 3)); // Str IV
                    if (data != null) {
                        data.restoreMana(data.maxMana());
                        AwakeningSystemMod.sync(player);
                    }
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 4800, 7)); // 16 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 4800, 1)); // Haste II
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 4800, 2)); // Speed III
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2400, 4)); // Regen V
                }
            }
            case ADAMANTITE -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 7200, 10)); // 22 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 7200, 4)); // Res V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 4)); // Str V
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3600, 5)); // Regen VI
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 8)); // 18 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 3)); // Res IV
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 4800, 3)); // Str IV
                }
            }
            case SPIRIT_JADE -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 7200, 11)); // 24 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 4800, 5)); // Regen VI
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 7200, 4)); // Res V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 7200, 4)); // Str V
                    if (data != null) {
                        data.restoreMana(data.maxMana());
                        data.restoreEnergy(100);
                        AwakeningSystemMod.sync(player);
                    }
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 9)); // 20 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3600, 5)); // Regen VI
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 3)); // Res IV
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 3)); // Str IV
                    if (data != null) {
                        data.restoreEnergy(50);
                        AwakeningSystemMod.sync(player);
                    }
                }
            }
            case ASTRAL -> {
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 9600, 12)); // 26 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 5)); // Regen VI
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 7200, 4)); // Str V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 9600, 4)); // Res V
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 9600, 0));
                    if (data != null) {
                        data.restoreMana(data.maxMana());
                        AwakeningSystemMod.sync(player);
                    }
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 7200, 10)); // 22 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 4800, 5)); // Regen VI
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 4)); // Str V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 7200, 4)); // Res V
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 7200, 0));
                }
            }
            case CELESTIAL -> {
                clearNegativeEffects(player);
                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 12000, 13)); // 28 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 7200, 5)); // Regen VI
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 9600, 4)); // Str V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12000, 4)); // Res V
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 2400, 0));
                    player.setHealth(player.getMaxHealth());
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 9600, 12)); // 26 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 5)); // Regen VI
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 7200, 4)); // Str V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 9600, 4)); // Res V
                    player.heal(20.0F);
                }
            }
            case PRIMORDIAL -> {
                clearNegativeEffects(player);
                player.setHealth(player.getMaxHealth());
                if (data != null) {
                    data.restoreMana(data.maxMana());
                    data.restoreEnergy(100);
                    AwakeningSystemMod.sync(player);
                }
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.SYSTEM_LEVEL_UP.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

                if (enchanted) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 14400, 14)); // 30 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 12000, 5)); // Regen VI
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 12000, 4)); // Str V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 14400, 4)); // Res V
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 14400, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 7200, 2)); // Speed III
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 12000, 14)); // 30 hearts
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 7200, 5)); // Regen VI
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 9600, 4)); // Str V
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12000, 4)); // Res V
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0));
                }
            }
        }
    }

    private static void clearNegativeEffects(ServerPlayer player) {
        List<MobEffectInstance> negativeEffects = player.getActiveEffects().stream()
                .filter(effect -> !effect.getEffect().value().isBeneficial())
                .toList();
        for (MobEffectInstance effect : negativeEffects) {
            player.removeEffect(effect.getEffect());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String encPrefix = enchanted ? "§6✦ Encantada ✦" : "§7Base";
        tooltip.add(Component.literal("§8Tier " + tier.tierNumber + " - " + encPrefix));
        tooltip.add(Component.literal("§7Nivel Requerido: §eNv. " + tier.reqLevel + "+"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system." + (enchanted ? "enchanted_" : "") + tier.idName + "_apple.desc"));
    }
}
