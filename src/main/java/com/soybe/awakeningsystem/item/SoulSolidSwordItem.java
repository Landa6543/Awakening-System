package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;

import java.util.List;

public class SoulSolidSwordItem extends SwordItem implements AwakeningWeaponItem {

    public SoulSolidSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 8, -1.6F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setTicksFrozen(200);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false, true));

        Level level = target.level();
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0D, target.getZ(), 16, 0.4D, 0.6D, 0.4D, 0.05D);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 0.8D, target.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.02D);
            sl.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.4F, 1.3F);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        player.getCooldowns().addCooldown(this, 160);

        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, Math.max(0.1D, look.y * 0.4D), look.z).normalize();

        player.setDeltaMovement(forward.scale(1.75D));
        player.hurtMarked = true;

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;

            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                    sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                    new com.soybe.awakeningsystem.SpawnMagicCirclePayload(
                            sp.getId(), sp.getX(), sp.getY(), sp.getZ(),
                            (float) forward.x, (float) forward.y, (float) forward.z,
                            2.8F, 30, 0x88EEFF, 1, false
                    )
            );

            sl.playSound(null, sp.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.8F, 1.6F);
            sl.playSound(null, sp.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 1.4F);
            sl.playSound(null, sp.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.6F, 1.2F);

            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 26.0D, 0.50F, 8);

            float dmg = (float) (24.0D * WeaponLevelManager.getDamageMultiplier(stack));

            SlashWaveEntity frostBeam = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_FROST_WIND, dmg, 3.4F, 1.3F, 0.0F, 22, 8);
            frostBeam.setDeltaMovement(forward.scale(2.4D));
            sl.addFreshEntity(frostBeam);

            WeaponLevelManager.addExp(stack, 30, sp);

            sp.displayClientMessage(Component.translatable("item.awakening_system.soul_solid.msg"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.awakening_system.soul_solid.subtitle").withStyle(ChatFormatting.ITALIC));
        tooltipComponents.add(Component.translatable("item.awakening_system.soul_solid.lore"));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("item.awakening_system.soul_solid.passive.title").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltipComponents.add(Component.translatable("item.awakening_system.soul_solid.passive.desc"));
        tooltipComponents.add(Component.translatable("item.awakening_system.soul_solid.active.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltipComponents.add(Component.translatable("item.awakening_system.soul_solid.active.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
