package com.soybe.awakeningsystem.item;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.soybe.awakeningsystem.CameraShakePayload;
import com.soybe.awakeningsystem.SpawnMagicCirclePayload;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class AdamantiteWarHammerItem extends SwordItem implements AwakeningWeaponItem {

    public AdamantiteWarHammerItem(Properties properties) {
        super(CustomToolTiers.ADAMANTITE, properties
                .fireResistant()
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(CustomToolTiers.ADAMANTITE, 8, -3.2F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if ((attacker.fallDistance > 0.0F || !attacker.onGround()) && !attacker.onClimbable() && !attacker.isInWater()) {
            if (attacker.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 0.8D, target.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.1D);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 0.8D, target.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.2D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.8F);

                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1, false, true));

                if (attacker instanceof Player p) {
                    p.displayClientMessage(Component.translatable("awakening_system.item.hammer.crushing_strike"), true);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cd = sp.getPersistentData().getLong("adamantite_hammer_cd");
            if (gameTime < cd) {
                long rem = (cd - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.hammer.seismic_cooldown", rem), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("adamantite_hammer_cd", gameTime + 120L);
            sp.getCooldowns().addCooldown(this, 120);

            if (!sp.onGround()) {
                sp.setDeltaMovement(0.0D, -1.8D, 0.0D);
                sp.hurtMarked = true;
                sp.getPersistentData().putBoolean("hammer_slam_diving", true);
                sp.getPersistentData().putInt("hammer_slam_timer", 30);
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2F, 0.5F);
                sp.displayClientMessage(Component.translatable("awakening_system.item.hammer.seismic_dive"), true);
            } else {
                performEarthquake(sp, (ServerLevel) level, 1.0F);
            }
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        if (level.isClientSide()) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                if (!player.onGround()) {
                    player.setDeltaMovement(0.0D, -1.8D, 0.0D);
                }
                player.getCooldowns().addCooldown(this, 120);
                return InteractionResultHolder.sidedSuccess(stack, true);
            }
            return InteractionResultHolder.fail(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide && isSelected && entity instanceof ServerPlayer sp) {
            if (sp.getPersistentData().getBoolean("hammer_slam_diving")) {
                int timer = sp.getPersistentData().getInt("hammer_slam_timer") - 1;
                sp.getPersistentData().putInt("hammer_slam_timer", timer);

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLAME, sp.getX(), sp.getY() + 0.3D, sp.getZ(), 4, 0.2D, 0.1D, 0.2D, 0.02D);
                    sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, sp.getX(), sp.getY() + 0.1D, sp.getZ(), 3, 0.2D, 0.1D, 0.2D, 0.01D);
                }

                if (sp.onGround() || sp.isInWater() || timer <= 0) {
                    sp.getPersistentData().remove("hammer_slam_diving");
                    sp.getPersistentData().remove("hammer_slam_timer");
                    performEarthquake(sp, (ServerLevel) level, 1.5F);
                }
            }
        }
    }

    public static void performEarthquake(ServerPlayer player, ServerLevel level, float powerMultiplier) {
        double radius = 7.0D * powerMultiplier;
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        level.playSound(null, px, py, pz, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 0.7F);
        level.playSound(null, px, py, pz, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.6F, 0.5F);
        level.playSound(null, px, py, pz, SoundEvents.MUD_BREAK, SoundSource.PLAYERS, 1.5F, 0.5F);

        PacketDistributor.sendToPlayersNear(
                level, null, px, py, pz, 64.0D,
                new SpawnMagicCirclePayload(
                        player.getId(), px, py + 0.05D, pz,
                        0.0F, 1.0F, 0.0F,
                        (float)(radius * 0.9F), 45, 0xFF4500, 0, false
                )
        );

        CameraShakePayload.sendToNearby(level, px, py, pz, radius + 16.0D, 0.95F * powerMultiplier, (int)(12 * powerMultiplier));

        for (double r = 1.0D; r <= radius; r += 1.2D) {
            int count = (int) (r * 8);
            for (int i = 0; i < count; i++) {
                double angle = (2.0D * Math.PI * i) / count;
                double fx = px + r * Math.cos(angle);
                double fz = pz + r * Math.sin(angle);
                level.sendParticles(ParticleTypes.LAVA, fx, py + 0.1D, fz, 1, 0, 0.05D, 0, 0.05D);
                level.sendParticles(ParticleTypes.EXPLOSION, fx, py + 0.1D, fz, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, fx, py + 0.1D, fz, 1, 0, 0.1D, 0, 0.02D);
            }
        }

        AABB quakeArea = player.getBoundingBox().inflate(radius, 3.0D, radius);
        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, quakeArea, e ->
                e.isAlive() && e != player && (e instanceof Enemy || !e.getTags().contains("shadow_soldier")));

        for (LivingEntity enemy : enemies) {
            enemy.hurt(enemy.damageSources().playerAttack(player), 20.0F * powerMultiplier);
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, true));
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true));

            Vec3 away = enemy.position().subtract(player.position()).normalize().scale(0.5D);
            enemy.setDeltaMovement(away.x, 0.65D * powerMultiplier, away.z);
            enemy.hurtMarked = true;
        }

        WeaponLevelManager.addExp(player.getMainHandItem(), (int)(25 * powerMultiplier), player);
        player.displayClientMessage(Component.translatable("awakening_system.item.hammer.earthquake", enemies.size()), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.adamantite_warhammer.lore"));
        tooltip.add(Component.translatable("item.awakening_system.adamantite_warhammer.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.adamantite_warhammer.passive"));
        tooltip.add(Component.translatable("item.awakening_system.adamantite_warhammer.active_title"));
        tooltip.add(Component.translatable("item.awakening_system.adamantite_warhammer.active_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.adamantite_warhammer.active_desc2"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
