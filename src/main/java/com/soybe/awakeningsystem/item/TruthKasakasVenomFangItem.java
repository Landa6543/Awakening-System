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
 * Truth Kasaka's Venom Fang (Colmillo Venenoso Verdadero de Kasaka).
 * La forma verdadera y despertada del Colmillo de Kasaka en Solo Leveling: Arise.
 * Cargada con una neurotoxina mutada de dragón serpentino que corroe los sentidos y paraliza a los enemigos.
 */
public class TruthKasakasVenomFangItem extends SwordItem implements AwakeningWeaponItem {

    public TruthKasakasVenomFangItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 7, -1.2F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            // Pasiva: Neurotoxina Verdadera (50% probabilidad de veneno severo y debilidad)
            if (attacker.getRandom().nextFloat() < 0.50F) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 2));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                sl.sendParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY() + 0.8D, target.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.06D);
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 0.9D, target.getZ(), 12, 0.25D, 0.3D, 0.25D, 0.04D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.5F);
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
        tooltip.add(Component.translatable("item.awakening_system.truth_kasakas_venom_fang.rank").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.truth_kasakas_venom_fang.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.truth_kasakas_venom_fang.origin").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.truth_kasakas_venom_fang.passive").withStyle(ChatFormatting.GREEN));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
