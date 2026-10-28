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

public class ScytheWeaponItem extends SwordItem {
    public ScytheWeaponItem(Tier tier, Properties properties, int attackDamageModifier, float attackSpeedModifier) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            if (getTier() == CustomToolTiers.SOUL_ALLOY) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 14, 0.35D, 0.35D, 0.35D, 0.05D);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.2D, target.getZ(), 6, 0.25D, 0.25D, 0.25D, 0.03D);
                target.setRemainingFireTicks(60);
            } else if (getTier() == CustomToolTiers.ASTRALITE) {
                sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.04D);
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 16, 0.35D, 0.35D, 0.35D, 0.06D);
            }

            // Mecánica activa de Guadaña: Barrido cosechador en arco 360°
            if (attacker instanceof net.minecraft.world.entity.player.Player player) {
                sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.85F);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 0.5D, target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);

                float sweepDamage = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * 0.75F;
                net.minecraft.world.phys.AABB sweepBox = target.getBoundingBox().inflate(3.2D, 1.0D, 3.2D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, sweepBox)) {
                    if (nearby != attacker && nearby != target && !attacker.isAlliedTo(nearby)) {
                        nearby.hurt(attacker.damageSources().playerAttack(player), sweepDamage);
                        double dx = nearby.getX() - attacker.getX();
                        double dz = nearby.getZ() - attacker.getZ();
                        nearby.knockback(0.45F, -dx, -dz);
                    }
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.awakening_system.weapon.scythe").withStyle(ChatFormatting.DARK_PURPLE));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
