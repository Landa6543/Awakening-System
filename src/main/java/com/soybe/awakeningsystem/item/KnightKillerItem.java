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
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Knight Killer (Asesina de Caballeros).
 * Daga de rango B comprada en la Tienda del Sistema en Solo Leveling.
 * Hoja dentada de alta dureza diseñada con penetración extrema para quebrar armaduras de placas pesadas.
 */
public class KnightKillerItem extends SwordItem implements AwakeningWeaponItem {

    public KnightKillerItem(Properties properties) {
        super(Tiers.DIAMOND, properties
                .rarity(Rarity.RARE)
                .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 4, -1.0F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            // Pasiva: Quiebra-Armaduras (daño directo adicional que ignora protección de placas)
            float extraPiercing = Math.min(10.0F, (float) target.getArmorValue() * 0.45F + 2.0F);
            target.hurt(attacker.damageSources().generic(), extraPiercing);
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 0.8D, target.getZ(), 10, 0.2D, 0.3D, 0.2D, 0.1D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ANVIL_HIT, SoundSource.PLAYERS, 0.7F, 1.8F);

            // Mecánica de Apuñalamiento por la Espalda
            Vec3 view = target.getViewVector(1.0F);
            Vec3 toAttacker = attacker.position().subtract(target.position()).normalize();
            if (view.dot(toAttacker) < -0.25D) {
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 0.9D, target.getZ(), 1, 0, 0, 0, 0);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.1F, 1.4F);
                if (attacker instanceof Player player) {
                    float bonus = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.35F;
                    target.hurt(attacker.damageSources().playerAttack(player), bonus);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.knight_killer.rank").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.knight_killer.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.knight_killer.origin").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.knight_killer.passive").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
