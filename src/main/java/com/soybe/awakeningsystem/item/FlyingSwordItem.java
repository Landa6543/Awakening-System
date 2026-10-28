package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.entity.FlyingSwordEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class FlyingSwordItem extends SwordItem implements AwakeningWeaponItem {
    public FlyingSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 8, -2.4F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.LivingEntity attacker) {
        if (attacker.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.03D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.1F, 1.4F);
            if (attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 3.0F);
            }
        }
        attacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 60, 0, false, false));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (sp.getVehicle() instanceof FlyingSwordEntity) {
                sp.stopRiding();
                return InteractionResultHolder.sidedSuccess(stack, false);
            }

            FlyingSwordEntity sword = ModEntities.FLYING_SWORD.get().create(level);
            if (sword != null) {
                sword.setPos(sp.getX(), sp.getY() + 0.1D, sp.getZ());
                sword.setYRot(sp.getYRot());
                sword.setXRot(sp.getXRot());
                level.addFreshEntity(sword);
                sp.startRiding(sword, true);

                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.2F, 1.2F);
                sp.displayClientMessage(Component.translatable("awakening_system.item.flying_sword.action_bar"), true);

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResultHolder.consume(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.item.flying_sword.tooltip_lore"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.item.flying_sword.tooltip_combat"));
        tooltip.add(Component.translatable("awakening_system.item.flying_sword.tooltip_flight"));
        tooltip.add(Component.translatable("awakening_system.item.flying_sword.tooltip_retrieve"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
