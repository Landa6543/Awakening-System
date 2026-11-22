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
 * Baruka's Dagger (Daga de Baruka).
 * Daga de hielo del señor elfo de hielo Baruka de la Puerta Roja en Solo Leveling.
 * Imbuida con el frío glaciar de los Elfos Blancos y cortes afilados de alta velocidad.
 */
public class BarukasDaggerItem extends SwordItem implements AwakeningWeaponItem {

    public BarukasDaggerItem(Properties properties) {
        super(Tiers.DIAMOND, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 5, -1.2F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            // Pasiva: Frío de Baruka (35% probabilidad de congelar y ralentizar)
            if (attacker.getRandom().nextFloat() < 0.35F) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 100, 260));
                sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 0.8D, target.getZ(), 16, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.9F, 1.4F);
            }

            // Mecánica de Apuñalamiento por la Espalda (Dagas)
            Vec3 view = target.getViewVector(1.0F);
            Vec3 toAttacker = attacker.position().subtract(target.position()).normalize();
            if (view.dot(toAttacker) < -0.25D) {
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.25D, 0.25D, 0.25D, 0.15D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.1F, 1.5F);
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
        tooltip.add(Component.translatable("item.awakening_system.barukas_dagger.rank").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.barukas_dagger.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.barukas_dagger.origin").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.barukas_dagger.passive").withStyle(ChatFormatting.BLUE));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
