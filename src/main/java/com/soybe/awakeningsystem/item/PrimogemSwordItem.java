package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class PrimogemSwordItem extends SwordItem {

    public PrimogemSwordItem() {
        super(CustomToolTiers.PRIMOGEM, new Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .attributes(SwordItem.createAttributes(CustomToolTiers.PRIMOGEM, 6.5F, -2.2F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && !attacker.level().isClientSide() && attacker.level() instanceof ServerLevel serverLevel) {
            boolean isCrit = attacker.fallDistance > 0.0F && !attacker.onGround() && !attacker.onClimbable()
                    && !attacker.isInWater() && !attacker.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
                    && !attacker.isPassenger();

            if (isCrit) {
                float extraDamage = 5.0F;
                target.hurt(attacker.damageSources().magic(), extraDamage);
                float healAmount = Math.max(1.0F, extraDamage * 0.4F);
                attacker.heal(healAmount);

                serverLevel.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.4, 0.4, 0.1);
                serverLevel.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 0.8, target.getZ(), 15, 0.3, 0.3, 0.3, 0.2);
                attacker.level().playSound(null, target.blockPosition(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 1.2F, 1.6F);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§5§l[HOJA VIVIENTE DEL DESTINO]").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.literal("§7Forjada con los cristales primordiales de los deseos de Teyvat."));
        tooltip.add(Component.literal("§d✦ Golpe Crítico: desata un estallido celestial que inflige daño mágico extra."));
        tooltip.add(Component.literal("§a✦ Drenaje Astral: absorbe la energía del impacto para curar tu vitalidad."));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
