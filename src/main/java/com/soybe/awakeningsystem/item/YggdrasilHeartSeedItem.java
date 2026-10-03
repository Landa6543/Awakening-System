package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
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

public class YggdrasilHeartSeedItem extends Item {
    public static final FoodProperties SEED_FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(1.2F)
            .alwaysEdible()
            .build();

    public YggdrasilHeartSeedItem(Properties properties) {
        super(properties.food(SEED_FOOD).rarity(Rarity.EPIC).fireResistant().stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            data.addCultivationQi(500);
            data.restoreEnergy(data.maxEnergy());
            data.addExperience(350);
            AwakeningSystemMod.sync(player);

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2400, 1, false, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2, player.getZ(), 60, 0.8, 1.0, 0.8, 0.15);
                sl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, player.getX(), player.getY() + 1.5, player.getZ(), 40, 1.0, 1.2, 1.0, 0.05);
                sl.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.6, 0.8, 0.6, 0.08);
            }

            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.4F, 1.0F);
            player.playNotifySound(SoundEvents.CHORUS_FLOWER_GROW, SoundSource.PLAYERS, 1.5F, 0.9F);

            AwakeningSystemMod.notify(player,
                    "awakening_system.item.yggdrasil_heart_seed.consumed_title",
                    "awakening_system.item.yggdrasil_heart_seed.consumed_desc",
                    0xFF22BB55);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.yggdrasil_heart_seed.desc"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.yggdrasil_heart_seed.effect"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
