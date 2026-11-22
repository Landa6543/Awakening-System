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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Moonshadow (Sombra Lunar).
 * Daga crescéntica oscura de rango SSR en Solo Leveling: Arise.
 * Forjada con sombras abisales y cortes veloces que envuelven a la víctima en un eclipse cegador.
 */
public class MoonshadowItem extends SwordItem implements AwakeningWeaponItem {

    public MoonshadowItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 8, -1.1F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            // Pasiva: Eclipse Lunar (oscuridad y ceguera al objetivo)
            if (attacker.getRandom().nextFloat() < 0.35F) {
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 0.9D, target.getZ(), 16, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.9F, 1.6F);
            }

            // Mecánica de Apuñalamiento por la Espalda
            Vec3 view = target.getViewVector(1.0F);
            Vec3 toAttacker = attacker.position().subtract(target.position()).normalize();
            if (view.dot(toAttacker) < -0.25D) {
                sl.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 14, 0.25D, 0.25D, 0.25D, 0.1D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.7F);
                if (attacker instanceof Player player) {
                    float bonus = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.45F;
                    target.hurt(attacker.damageSources().playerAttack(player), bonus);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.moonshadow.rank").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.moonshadow.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.moonshadow.origin").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.moonshadow.passive").withStyle(ChatFormatting.LIGHT_PURPLE));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
