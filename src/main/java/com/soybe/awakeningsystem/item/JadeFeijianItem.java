package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.entity.JadeFeijianEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;

public class JadeFeijianItem extends SwordItem implements AwakeningWeaponItem {

    public JadeFeijianItem(Properties properties) {
        super(CustomToolTiers.SPIRIT_JADE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(CustomToolTiers.SPIRIT_JADE, 2, -2.2F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.1D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.7F);
            if (attacker instanceof Player p) {
                target.hurt(target.damageSources().magic(), 4.0F); // Daño espiritual de Qi de Jade
            }
        }
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 0, false, false));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(held);
        }

        player.getCooldowns().addCooldown(this, 20);

        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();
            ItemStack singleSword = held.copyWithCount(1);
            JadeFeijianEntity feijian = new JadeFeijianEntity(level, player, singleSword);
            feijian.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
            feijian.shoot(look.x, look.y, look.z, 1.4F, 0.2F);
            level.addFreshEntity(feijian);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 1.6F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.6F);

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.jade_feijian.lore"));
        tooltip.add(Component.translatable("item.awakening_system.jade_feijian.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.jade_feijian.right_click"));
        tooltip.add(Component.translatable("item.awakening_system.jade_feijian.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.jade_feijian.desc2"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
