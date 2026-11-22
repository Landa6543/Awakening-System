package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Blue Monsoon (Monzón Azul).
 * Lanza de agua torrencial de Solo Leveling: Arise imbuida con la furia de las mareas y torrentes marinos.
 */
public class BlueMonsoonItem extends SwordItem implements AwakeningWeaponItem {

    public BlueMonsoonItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 7, -2.2F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            // Pasiva: Oleada Monzónica (25% probabilidad de estallido de agua en área)
            if (attacker.getRandom().nextFloat() < 0.25F) {
                sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 0.8D, target.getZ(), 45, 0.6D, 0.5D, 0.6D, 0.15D);
                sl.sendParticles(ParticleTypes.BUBBLE, target.getX(), target.getY() + 0.6D, target.getZ(), 20, 0.5D, 0.4D, 0.5D, 0.05D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.2F, 1.1F);

                AABB waveBox = target.getBoundingBox().inflate(3.0D, 1.0D, 3.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, waveBox, e -> e != attacker && e.isAlive())) {
                    if (nearby != target) {
                        nearby.hurt(attacker.damageSources().mobAttack(attacker), 6.0F);
                    }
                    nearby.knockback(0.6D, nearby.getX() - attacker.getX(), nearby.getZ() - attacker.getZ());
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.blue_monsoon.rank").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.blue_monsoon.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.blue_monsoon.origin").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.blue_monsoon.passive").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
