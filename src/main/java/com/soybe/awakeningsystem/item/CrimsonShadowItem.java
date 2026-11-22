package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Crimson Shadow (Sombra Carmesí).
 * Imponente mandoble llameante forjado con esencia carmesí y llamas umbrías.
 * Desata devastadoras hendeduras ígneas en área que abrasan a múltiples enemigos.
 */
public class CrimsonShadowItem extends SwordItem implements AwakeningWeaponItem {

    public CrimsonShadowItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 8, -2.4F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            // Ignición directa al objetivo
            target.igniteForSeconds(5);
            sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.05D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.85F, 1.2F);

            // Barrido de Fuego Carmesí en área (mandoble / greatsword cleave)
            AABB area = target.getBoundingBox().inflate(3.0D, 1.5D, 3.0D);
            List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != attacker && e != target && e instanceof Enemy && e.isAlive());

            for (LivingEntity nearbyEnemy : nearby) {
                nearbyEnemy.igniteForSeconds(4);
                if (attacker instanceof Player player) {
                    float cleaveDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.45F;
                    nearbyEnemy.hurt(attacker.damageSources().playerAttack(player), cleaveDmg);
                }
                sl.sendParticles(ParticleTypes.LAVA, nearbyEnemy.getX(), nearbyEnemy.getY() + 0.8D, nearbyEnemy.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.02D);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.crimson_shadow.rank").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.crimson_shadow.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.crimson_shadow.origin").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.crimson_shadow.passive").withStyle(ChatFormatting.RED));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
