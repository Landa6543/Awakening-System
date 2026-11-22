package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Demon King's Daggers (Dagas del Rey Demonio).
 * Dagas legendarias forjadas a partir de los cuernos de Baran, el Monarca de las Llamas Blancas.
 * Portan la furia del relámpago celestial y ráfagas de voltios destructores.
 */
public class DemonKingsDaggersItem extends SwordItem implements AwakeningWeaponItem {

    public DemonKingsDaggersItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 9, -1.2F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            // Pasiva: Relámpago de Baran (30% probabilidad de chispas en cadena)
            if (attacker.getRandom().nextFloat() < 0.30F) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.9D, target.getZ(), 25, 0.4D, 0.5D, 0.4D, 0.1D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.9F, 1.8F);

                AABB chainArea = target.getBoundingBox().inflate(3.5D, 1.2D, 3.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, chainArea, e -> e != attacker && e.isAlive())) {
                    if (nearby != target) {
                        nearby.hurt(attacker.damageSources().lightningBolt(), 7.0F);
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, nearby.getX(), nearby.getY() + 0.7D, nearby.getZ(), 10, 0.2D, 0.3D, 0.2D, 0.05D);
                    }
                }
            }

            // Mecánica de Apuñalamiento por la Espalda
            Vec3 view = target.getViewVector(1.0F);
            Vec3 toAttacker = attacker.position().subtract(target.position()).normalize();
            if (view.dot(toAttacker) < -0.25D) {
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 16, 0.25D, 0.25D, 0.25D, 0.15D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.6F);
                if (attacker instanceof Player player) {
                    float bonus = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.50F;
                    target.hurt(attacker.damageSources().playerAttack(player), bonus);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.demon_kings_daggers.rank").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.demon_kings_daggers.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.demon_kings_daggers.origin").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.demon_kings_daggers.passive").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
