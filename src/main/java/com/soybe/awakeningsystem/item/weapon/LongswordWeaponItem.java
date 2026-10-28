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

public class LongswordWeaponItem extends SwordItem {
    public LongswordWeaponItem(Tier tier, Properties properties, int attackDamageModifier, float attackSpeedModifier) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            if (getTier() == CustomToolTiers.SOUL_ALLOY) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.04D);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.2D, target.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.02D);
            } else if (getTier() == CustomToolTiers.ASTRALITE) {
                sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.25D, 0.25D, 0.25D, 0.03D);
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
            }
            // Mecánica activa de Espada Larga: Tajo amplio con barrido medio
            sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.PLAYERS, 0.9F, 1.1F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.awakening_system.weapon.longsword").withStyle(ChatFormatting.BLUE));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
