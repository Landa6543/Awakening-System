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
 * Fruta espiritual de los legendarios ciruelos en flor de la Secta de Monte Hua.
 * Otorga vitalidad y Qi de cultivo.
 */
public class SpiritPlumFruitItem extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.8F)
            .alwaysEdible()
            .build();

    public SpiritPlumFruitItem(Properties properties) {
        super(properties.food(FOOD).rarity(Rarity.UNCOMMON).stacksTo(64));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data != null) {
                data.addCultivationQi(30);
                data.addExperience(40);
                AwakeningSystemMod.sync(player);
            }

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.6D, 0.6D, 0.6D, 0.08D);
                sl.sendParticles(ParticleTypes.COMPOSTER, player.getX(), player.getY() + 0.5D, player.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.05D);
            }

            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
            player.displayClientMessage(Component.translatableWithFallback("item.awakening_system.spirit_plum_fruit.msg", "§d§l🌸 [SPIRIT PLUM] §fThe nectar of Mount Hua purifies your meridians (+30 Qi)."), true);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.spirit_plum_fruit.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.spirit_plum_fruit.desc"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.spirit_plum_fruit.on_consume"));
        tooltip.add(Component.translatable("item.awakening_system.spirit_plum_fruit.effect1"));
        tooltip.add(Component.translatable("item.awakening_system.spirit_plum_fruit.effect2"));
        tooltip.add(Component.translatable("item.awakening_system.spirit_plum_fruit.harvest_hint"));
    }
}
