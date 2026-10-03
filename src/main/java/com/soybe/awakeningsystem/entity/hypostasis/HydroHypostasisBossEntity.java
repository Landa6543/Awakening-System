package com.soybe.awakeningsystem.entity.hypostasis;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HydroHypostasisBossEntity extends HypostasisBaseBossEntity {

    public HydroHypostasisBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(getBossTitle());
        this.setCustomNameVisible(true);
    }

    @Override
    protected Component getBossTitle() {
        return Component.literal("§9✦ He — Hipóstasis Hydro ✦");
    }

    @Override
    protected BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.BLUE;
    }

    @Override
    public ParticleOptions getElementalParticle() {
        return ParticleTypes.SPLASH;
    }

    @Override
    public int getElementalColorHex() {
        return 0x0077FF; // Azul Profundo Hydro
    }

    @Override
    protected void executeAttackStep(ServerLevel sl, Player target, HypostasisAttackType attack, float progress) {
        Vec3 toTarget = target.position().subtract(this.position()).normalize();

        switch (attack) {
            case CLAP -> {
                if (progress >= 0.58F && progress <= 0.65F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 2.0F, 1.0F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.4F, 0.8F);

                    Vec3 strikePos = this.position().add(toTarget.scale(2.6D));
                    sl.sendParticles(ParticleTypes.SPLASH, strikePos.x, strikePos.y, strikePos.z, 80, 1.2D, 1.0D, 1.2D, 0.2D);
                    sl.sendParticles(ParticleTypes.BUBBLE, strikePos.x, strikePos.y, strikePos.z, 40, 1.0D, 0.8D, 1.0D, 0.1D);

                    AABB cone = new AABB(strikePos.x - 3.8D, strikePos.y - 1.5D, strikePos.z - 3.8D,
                            strikePos.x + 3.8D, strikePos.y + 3.0D, strikePos.z + 3.8D);

                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, cone)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().mobAttack(this), 26.0F);
                            nearby.setDeltaMovement(toTarget.x * 1.5D, 0.8D, toTarget.z * 1.5D);
                        }
                    }
                }
            }
            case MISSILES -> {
                int step = (int) (progress * 10);
                if (progress > 0.25F && progress < 0.85F && (step % 2 == 0)) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.HOSTILE, 0.8F, 1.4F);
                    Vec3 targetArea = target.position().add((rng.nextDouble() - 0.5D) * 2.0D, 0, (rng.nextDouble() - 0.5D) * 2.0D);
                    sl.sendParticles(ParticleTypes.FALLING_WATER, targetArea.x, targetArea.y + 0.2D, targetArea.z, 25, 0.4D, 0.4D, 0.4D, 0.1D);

                    AABB hitBox = new AABB(targetArea.x - 1.2D, targetArea.y - 0.5D, targetArea.z - 1.2D,
                            targetArea.x + 1.2D, targetArea.y + 2.0D, targetArea.z + 1.2D);

                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, hitBox)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().mobAttack(this), 12.0F);
                        }
                    }
                }
            }
            case SPECIAL_ACTION, BURST_ULTIMATE -> {
                if (progress >= 0.40F && progress <= 0.45F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 2.0F, 0.7F);
                    double radius = 7.0D;
                    for (int i = 0; i < 36; i++) {
                        double angle = Math.toRadians(i * 10);
                        double px = this.getX() + Math.cos(angle) * radius;
                        double pz = this.getZ() + Math.sin(angle) * radius;
                        sl.sendParticles(ParticleTypes.SPLASH, px, this.getY() + 0.2D, pz, 6, 0.1D, 0.1D, 0.1D, 0.1D);
                    }
                    AABB waveBox = this.getBoundingBox().inflate(radius, 2.0D, radius);
                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, waveBox)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().mobAttack(this), 22.0F);
                            nearby.setDeltaMovement(0, 0.7D, 0);
                        }
                    }
                }
            }
            default -> {}
        }
    }

    @Override
    protected void spawnRevivalObjects(ServerLevel sl) {
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 2.0F, 0.8F);
        for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
            player.displayClientMessage(Component.literal("§9§l✦ [HE EN CRISIS] §f¡Interrumpe las gotas de agua con ataques elementales antes de que curen al núcleo!"), true);
        }
    }

    @Override
    protected boolean checkRevivalObjectsCleared(ServerLevel sl) {
        return true;
    }

    @Override
    protected void cleanupRevivalObjects(ServerLevel sl) {}

    @Override
    protected void dropBossLoot(ServerLevel sl) {
        boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
        for (Player p : sl.getPlayers(pl -> pl.distanceToSqr(this) < 1600.0D)) {
            if (p instanceof ServerPlayer sp) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(60000);
                    data.addPrimogems(30);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    sp.displayClientMessage(Component.literal("§9§l✦ ¡HIPÓSTASIS HYDRO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e."), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                    }
                    sp.displayClientMessage(Component.literal("§9§l✦ ¡HIPÓSTASIS HYDRO DERROTADO! §6+24 Monedas de Despertar§e."), false);
                }
            }
        }

        if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.PRIMOGEM.get(), 25)));
        }
        sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2)));
        if (SystemItems.DEW_OF_REPUDIATION.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.DEW_OF_REPUDIATION.get(), 2 + random.nextInt(2))));
        }
        if (SystemItems.HYDRO_HYPOSTASIS_BLOCK.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.HYDRO_HYPOSTASIS_BLOCK.get(), 1)));
        }
        if (SystemItems.VARUNADA_LAZURITE_GEMSTONE.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.VARUNADA_LAZURITE_GEMSTONE.get(), 1)));
        }
        if (SystemItems.VISION_HYDRO.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.VISION_HYDRO.get(), 1)));
        }
    }

    @Override
    protected void handleClientParticles() {
        if (random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.FALLING_WATER,
                    this.getX() + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getY() + 1.0D + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 1.5D,
                    0, -0.05D, 0);
        }
    }
}
