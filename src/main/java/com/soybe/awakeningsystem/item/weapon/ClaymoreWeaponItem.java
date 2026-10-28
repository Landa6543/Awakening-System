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

public class ClaymoreWeaponItem extends SwordItem {
    public ClaymoreWeaponItem(Tier tier, Properties properties, int attackDamageModifier, float attackSpeedModifier) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            if (getTier() == CustomToolTiers.SOUL_ALLOY) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 16, 0.4D, 0.4D, 0.4D, 0.06D);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.2D, target.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.04D);
            } else if (getTier() == CustomToolTiers.ASTRALITE) {
                sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.35D, 0.35D, 0.35D, 0.05D);
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 18, 0.4D, 0.4D, 0.4D, 0.07D);
            }
            // Mecánica activa de Mandoble: Impacto demoledor con aturdimiento / lentitud pesada
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
            sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.ANVIL_LAND, net.minecraft.sounds.SoundSource.PLAYERS, 0.35F, 1.6F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.awakening_system.weapon.claymore").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
