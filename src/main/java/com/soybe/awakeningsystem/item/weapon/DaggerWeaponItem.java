package com.soybe.awakeningsystem.item.weapon;

import com.soybe.awakeningsystem.item.CustomToolTiers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class DaggerWeaponItem extends SwordItem {
    public DaggerWeaponItem(Tier tier, Properties properties, int attackDamageModifier, float attackSpeedModifier) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            if (getTier() == CustomToolTiers.SOUL_ALLOY) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 0.8D, target.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.03D);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 3, 0.15D, 0.15D, 0.15D, 0.02D);
            } else if (getTier() == CustomToolTiers.ASTRALITE) {
                sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 0.8D, target.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.03D);
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 0.8D, target.getZ(), 8, 0.25D, 0.25D, 0.25D, 0.04D);
            }

            // Mecánica activa de Daga: Puñalada por la espalda (Backstab)
            net.minecraft.world.phys.Vec3 view = target.getViewVector(1.0F);
            net.minecraft.world.phys.Vec3 toAttacker = attacker.position().subtract(target.position()).normalize();
            if (view.dot(toAttacker) < -0.25D) {
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.25D, 0.25D, 0.25D, 0.15D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT, net.minecraft.sounds.SoundSource.PLAYERS, 1.1F, 1.3F);
                if (attacker instanceof net.minecraft.world.entity.player.Player player) {
                    float bonus = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * 0.45F;
                    target.hurt(attacker.damageSources().playerAttack(player), bonus);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.awakening_system.weapon.dagger").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
