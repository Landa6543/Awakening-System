package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
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

/**
 * Melocotón milenario de la longevidad e inmortalidad celestial de Murim (Pantao / 蟠桃).
 * Fruto supremo que purifica el alma, cura venenos y desata un inmenso flujo de Qi.
 */
public class HeavenlyLongevityPeachItem extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(1.4F)
            .alwaysEdible()
            .build();

    public HeavenlyLongevityPeachItem(Properties properties) {
        super(properties.food(FOOD).rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data != null) {
                data.addCultivationQi(150);
                data.addExperience(100);
                data.restoreEnergy(80);
                AwakeningSystemMod.sync(player);
            }

            // Purificación absoluta de estados nocivos
            player.removeAllEffects();

            // Bendiciones supremas de longevidad
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2, false, true)); // Regen III (20s)
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3, false, true)); // Absorción IV (30s)
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1, false, true)); // Resistencia II (30s)
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0, false, true)); // Inmune fuego 60s

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 60, 0.8D, 1.0D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 2, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 1.2D, 1.0D, 1.2D, 0.1D);
            }

            player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
            player.displayClientMessage(Component.literal("§6§l🍑 [MELOCOTÓN DE LA INMORTALIDAD] §e¡La fruta divina regenera tu cuerpo y meridianos (+150 Qi Supremo)!"), true);
        }
        return result;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Resplandor dorado de reliquia viva
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6§lMelocotón Celestial de la Inmortalidad (Pantao)").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§dFruta mítica del Jardín Celestial que madura solo una vez cada tres mil años."));
        tooltip.add(Component.literal("§7Concede longevidad, cura todo mal y nutre el Mar de Qi del cultivador."));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§e▪ Al Consumir:"));
        tooltip.add(Component.literal("  §b▪ +150 Qi Supremo de Cultivo, +100 EXP y +80 Energía"));
        tooltip.add(Component.literal("  §a▪ Purificación Total de Maleficios y Efectos Negativos"));
        tooltip.add(Component.literal("  §6▪ Regeneración III (20s), Absorción IV (30s) y Resistencia II (30s)"));
    }
}
