package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SpawnMagicCirclePayload;
import com.soybe.awakeningsystem.CameraShakePayload;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class DreadScytheItem extends SwordItem implements AwakeningWeaponItem {

    public DreadScytheItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .fireResistant()
                .durability(45000)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 25, -2.8F)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            long gameTime = level.getGameTime();

            long cdUntil = sp.getPersistentData().getLong("dread_scythe_skill_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.dread_scythe.cd", "§5§l☠ [Vortex of Dread] on cooldown: §f%ss", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("dread_scythe_skill_cd", gameTime + 600L);
            sp.getCooldowns().addCooldown(this, 600);

            PacketDistributor.sendToPlayersTrackingEntityAndSelf(sp, new SpawnMagicCirclePayload(
                    sp.getId(),
                    sp.getX(), sp.getY() + 0.05D, sp.getZ(),
                    0.0F, 1.0F, 0.0F,
                    12.0F, 50, 0xFF8A2BE2, 0, false
            ));

            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.5F, 1.4F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 2.0F, 0.8F);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 1.5F);

            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 100, 5.0D, 1.5D, 5.0D, 0.15D);
            sl.sendParticles(ParticleTypes.SOUL, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 60, 4.0D, 1.5D, 4.0D, 0.1D);
            sl.sendParticles(ParticleTypes.WITCH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 80, 5.0D, 1.5D, 5.0D, 0.2D);
            sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 120, 6.0D, 2.0D, 6.0D, 0.3D);

            AABB area = sp.getBoundingBox().inflate(12.0D, 4.0D, 12.0D);
            int hitCount = 0;
            for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, area)) {
                if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                    nearby.hurt(nearby.damageSources().playerAttack(sp), 58.0F);

                    Vec3 pull = sp.position().subtract(nearby.position()).normalize().scale(1.1D);
                    nearby.setDeltaMovement(pull.x, 0.5D, pull.z);

                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
                    nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 1));
                    nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, nearby.getX(), nearby.getY() + 1.0D, nearby.getZ(), 1, 0, 0, 0, 0);
                    hitCount++;
                }
            }

            sp.heal(6.0F + (hitCount * 1.5F));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 2, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true));

            CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 36.0D, 0.85F, 12);

            Vec3 look = sp.getLookAngle();
            SlashWaveEntity scytheWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, 60.0F, 5.4F, 2.0F, -30.0F, 28, 12);
            scytheWave.setDeltaMovement(look.scale(2.2D));
            sl.addFreshEntity(scytheWave);

            WeaponLevelManager.addExp(stack, 40, sp);

            sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.dread_scythe.vortex_msg", "§5§l☠ [VORTEX OF DREAD] §dHarvested the souls of %s enemies! (+Health restored)", hitCount), true);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            if (attacker instanceof Player player) {
                target.hurt(target.damageSources().playerAttack(player), 12.0F);
                player.heal(2.0F);

                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));

                AABB sweepArea = target.getBoundingBox().inflate(5.0D, 1.5D, 5.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, sweepArea)) {
                    if (nearby != target && nearby != attacker && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(player), 11.0F);
                        nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                    }
                }

                if (!target.isAlive() || target.getHealth() <= 0) {
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.8F, 1.2F);
                    sl.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.8D, 1.0D, 0.8D, 0.1D);
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 1, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 1, false, true));
                    player.displayClientMessage(Component.translatable("item.awakening_system.dread_scythe.msg"), true);
                }
            }

            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.2F, 1.6F);
            sl.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.4D, 0.5D, 0.4D, 0.1D);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.4D, 0.3D, 0.08D);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.header").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.desc2"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.attr_header"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.attr_dmg"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.attr_speed"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.attr_dura"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.passive_header"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.passive_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.passive_desc2"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.passive_desc3"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.active_header"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.active_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.active_desc2"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.active_desc3"));
        tooltip.add(Component.translatable("item.awakening_system.dread_scythe.active_cd"));
    }
}
