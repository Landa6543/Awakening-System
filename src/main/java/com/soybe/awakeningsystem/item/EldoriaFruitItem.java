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
 * Fruta estelar del Árbol Sagrado de Eldoria en Isekai.
 * Brilla con luz astral y nutre la energía de maná del usuario.
 */
public class EldoriaFruitItem extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.9F)
            .alwaysEdible()
            .build();

    public EldoriaFruitItem(Properties properties) {
        super(properties.food(FOOD).rarity(Rarity.RARE).stacksTo(64));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data != null) {
                data.restoreEnergy(50);
                data.addExperience(35);
                AwakeningSystemMod.sync(player);
            }

            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1, false, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 25, 0.5D, 0.6D, 0.5D, 0.08D);
                sl.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 0.6D, player.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.05D);
            }

            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.6F);
            player.displayClientMessage(Component.translatableWithFallback("item.awakening_system.eldoria_fruit.msg", "§e§l⭐ [ELDORIA FRUIT] §fThe astral essence sharpens your vision and restores +50 Mana."), true);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.eldoria_fruit.title").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.eldoria_fruit.desc"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.eldoria_fruit.on_consume"));
        tooltip.add(Component.translatable("item.awakening_system.eldoria_fruit.effect1"));
        tooltip.add(Component.translatable("item.awakening_system.eldoria_fruit.effect2"));
    }
}
