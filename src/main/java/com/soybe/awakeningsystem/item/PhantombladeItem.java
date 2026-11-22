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

import java.util.List;

/**
 * Phantomblade (Hoja Fantasma).
 * Espada radiante fantasmal de elemento Luz en Solo Leveling: Arise.
 * Destaca por sus cortes de alta frecuencia luminosa que purifican e iluminan el campo de batalla.
 */
public class PhantombladeItem extends SwordItem implements AwakeningWeaponItem {

    public PhantombladeItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 7, -2.0F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 0.9D, target.getZ(), 18, 0.3D, 0.4D, 0.3D, 0.08D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.6F);

            // Pasiva: Purificación Espectral (daño extra a no-muertos y resplandor)
            if (target.isInvertedHealAndHarm()) {
                target.hurt(attacker.damageSources().magic(), 6.0F);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                sl.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.phantomblade.rank").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.phantomblade.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.phantomblade.origin").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.phantomblade.passive").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
