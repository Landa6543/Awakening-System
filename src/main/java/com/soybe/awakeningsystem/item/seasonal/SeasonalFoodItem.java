package com.soybe.awakeningsystem.item.seasonal;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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

public class SeasonalFoodItem extends Item {

    public enum SweetType {
        GINGERBREAD_COOKIE("gingerbread_cookie", 6, 0.8F, "§eGalleta de Jengibre Real", "§7Regeneración de salud y saciedad prolongada."),
        CANDY_CANE("candy_cane", 4, 0.6F, "§cBastón de Caramelo Mágico", "§7Velocidad y prisa para maniobras ágiles."),
        HOT_CHOCOLATE("hot_chocolate", 6, 1.0F, "§6Chocolate Caliente Gourmet", "§7Calor reconfortante y absorción de daño."),
        ALMOND_NOUGAT("almond_nougat", 8, 1.2F, "§fTurrón Imperial de Almendra", "§7Resistencia formidable a ataques físicos."),
        POLAR_PEPPERMINT("polar_peppermint", 3, 0.5F, "§bCaramelo de Menta Polar", "§7Aliento gélido y velocidad invernal."),
        SOUL_CANDY("soul_candy", 4, 0.5F, "§bCaramelo de Alma", "§7Visión espectral y agilidad de ultratumba."),
        MANA_GUMMY("mana_gummy", 5, 0.7F, "§9Gomitas de Maná", "§7Regeneración inmediata de energía y maná."),
        BERSERKER_CHOCOLATE("berserker_chocolate", 6, 0.9F, "§4Chocolatín Berserker", "§7Furia sanguinaria: Fuerza bruta aumentada."),
        HAUNTED_CANDY("haunted_candy", 4, 0.4F, "§5Dulce Embrujado", "§7Ruleta de truco o trato: efectos sorpresa.");

        public final String id;
        public final int nutrition;
        public final float saturation;
        public final String displayName;
        public final String effectDesc;

        SweetType(String id, int nutrition, float saturation, String displayName, String effectDesc) {
            this.id = id;
            this.nutrition = nutrition;
            this.saturation = saturation;
            this.displayName = displayName;
            this.effectDesc = effectDesc;
        }

        public boolean isChristmas() {
            return this == GINGERBREAD_COOKIE || this == CANDY_CANE || this == HOT_CHOCOLATE || this == ALMOND_NOUGAT || this == POLAR_PEPPERMINT;
        }

        public boolean isHalloween() {
            return this == SOUL_CANDY || this == MANA_GUMMY || this == BERSERKER_CHOCOLATE || this == HAUNTED_CANDY;
        }

        public boolean isSeasonActive() {
            return isChristmas() ? com.soybe.awakeningsystem.seasonal.SeasonalEventManager.isChristmasActive()
                    : com.soybe.awakeningsystem.seasonal.SeasonalEventManager.isHalloweenActive();
        }

        public FoodProperties toFoodProperties() {
            return new FoodProperties.Builder()
                    .nutrition(nutrition)
                    .saturationModifier(saturation)
                    .alwaysEdible()
                    .build();
        }
    }

    private final SweetType sweetType;

    public SeasonalFoodItem(SweetType sweetType, Properties properties) {
        super(properties.food(sweetType.toFoodProperties()).rarity(Rarity.UNCOMMON));
        this.sweetType = sweetType;
    }

    public SweetType getSweetType() {
        return sweetType;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            if (!sweetType.isSeasonActive()) {
                player.displayClientMessage(Component.literal("§c§l[FESTIVIDAD EN LETARGO] §7Este manjar mágico solo despierta durante su festividad correspondiente."), true);
                return result;
            }

            switch (sweetType) {
                case GINGERBREAD_COOKIE -> {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0));
                }
                case CANDY_CANE -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 1));
                }
                case HOT_CHOCOLATE -> {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 900, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 900, 0));
                    player.clearFire();
                }
                case ALMOND_NOUGAT -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0));
                }
                case POLAR_PEPPERMINT -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0));
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1.0D, player.getZ(),
                                15, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                }
                case SOUL_CANDY -> {
                    player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                }
                case MANA_GUMMY -> {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                    SystemData data = SystemEvents.data(player);
                    if (data != null) {
                        data.addMana(50);
                    }
                }
                case BERSERKER_CHOCOLATE -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 0));
                }
                case HAUNTED_CANDY -> {
                    if (level.random.nextBoolean()) {
                        // Trick!
                        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0));
                        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 1));
                        player.sendSystemMessage(Component.literal("§d§l[TRUCO O TRATO] §f¡Flotando hacia la luna con fortuna festiva!"));
                    } else {
                        // Treat!
                        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 3));
                        player.sendSystemMessage(Component.literal("§a§l[TRUCO O TRATO] §f¡Disfraz de sombra fantasmal e hipervelocidad!"));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(sweetType.displayName));
        tooltip.add(Component.literal(sweetType.effectDesc));
        if (!sweetType.isSeasonActive()) {
            tooltip.add(Component.literal("§c§l[INACTIVO FUERA DE TEMPORADA]"));
            tooltip.add(Component.literal(sweetType.isChristmas() ? "§7Disponible únicamente del 1 de Dic al 7 de Ene." : "§7Disponible únicamente del 15 de Oct al 5 de Nov."));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
