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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DendroHypostasisBossEntity extends HypostasisBaseBossEntity {

    private final List<DendroRevivalPithEntity> activePiths = new ArrayList<>();

    public DendroHypostasisBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(getBossTitle());
        this.setCustomNameVisible(true);
    }

    @Override
    protected Component getBossTitle() {
        return Component.translatable("boss.awakening_system.hypostasis.dendro");
    }

    @Override
    protected BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.GREEN;
    }

    @Override
    public ParticleOptions getElementalParticle() {
        return ParticleTypes.HAPPY_VILLAGER;
    }

    @Override
    public int getElementalColorHex() {
        return 0x77E834;
    }

    @Override
    protected void executeAttackStep(ServerLevel sl, Player target, HypostasisAttackType attack, float progress) {
        Vec3 toTarget = target.position().subtract(this.position()).normalize();

        switch (attack) {
            case CLAP -> {
                if (progress >= 0.58F && progress <= 0.65F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GRASS_BREAK, SoundSource.HOSTILE, 1.8F, 1.0F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2F, 1.6F);

                    Vec3 hitPos = this.position().add(toTarget.scale(3.5D));
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, hitPos.x, hitPos.y + 0.5D, hitPos.z, 30, 1.2D, 1.0D, 1.2D, 0.1D);
                    sl.sendParticles(ParticleTypes.COMPOSTER, hitPos.x, hitPos.y + 0.5D, hitPos.z, 20, 1.0D, 0.8D, 1.0D, 0.05D);

                    AABB hitArea = new AABB(hitPos.x - 2.5D, hitPos.y - 1.5D, hitPos.z - 2.5D, hitPos.x + 2.5D, hitPos.y + 2.5D, hitPos.z + 2.5D);
                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, hitArea)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().mobAttack(this), 28.0F);
                            nearby.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                            nearby.knockback(1.0D, -toTarget.x, -toTarget.z);
                        }
                    }
                }
            }
            case MISSILES -> {
                if (progress >= 0.35F && progress <= 0.75F && (this.tickCount % 6 == 0)) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.HOSTILE, 1.0F, 1.3F);

                    double spreadX = (random.nextDouble() - 0.5D) * 1.5D;
                    double spreadZ = (random.nextDouble() - 0.5D) * 1.5D;
                    Vec3 sporePos = this.position().add(0, 1.2D, 0);
                    Vec3 dir = target.position().add(spreadX, 0.5D, spreadZ).subtract(sporePos).normalize().scale(0.85D);

                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sporePos.x, sporePos.y, sporePos.z, 10, 0.2D, 0.2D, 0.2D, 0.05D);

                    if (target.distanceToSqr(this) < 400.0D) {
                        target.hurt(target.damageSources().indirectMagic(this, this), 10.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                    }
                }
            }
            case SPECIAL_ACTION -> {
                if (progress >= 0.20F && progress <= 0.25F) {
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ROOTS_BREAK, SoundSource.HOSTILE, 1.5F, 1.2F);
                    sl.sendParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY() + 0.2D, target.getZ(), 40, 1.5D, 0.5D, 1.5D, 0.1D);

                    target.hurt(target.damageSources().mobAttack(this), 22.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                }
            }
            case BURST_ULTIMATE -> {
                if (progress >= 0.10F && progress <= 0.80F && (this.tickCount % 4 == 0)) {
                    double radius = progress * 10.0D;
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.CHORUS_FLOWER_GROW, SoundSource.HOSTILE, 1.2F, 1.5F);

                    for (int i = 0; i < 16; i++) {
                        double angle = (i / 16.0D) * Math.PI * 2;
                        double px = this.getX() + Math.cos(angle) * radius;
                        double pz = this.getZ() + Math.sin(angle) * radius;
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, this.getY() + 0.3D, pz, 3, 0.2D, 0.3D, 0.2D, 0.02D);
                        sl.sendParticles(ParticleTypes.COMPOSTER, px, this.getY() + 0.3D, pz, 2, 0.2D, 0.2D, 0.2D, 0.01D);
                    }

                    AABB shockArea = this.getBoundingBox().inflate(radius);
                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, shockArea)) {
                        if (nearby != this && nearby.distanceTo(this) <= radius) {
                            nearby.hurt(nearby.damageSources().mobAttack(this), 24.0F);
                            nearby.knockback(0.8D, nearby.getX() - this.getX(), nearby.getZ() - this.getZ());
                        }
                    }
                }
            }
            default -> {}
        }
    }

    @Override
    protected void spawnRevivalObjects(ServerLevel sl) {
        this.cleanupRevivalObjects(sl);

        for (int i = 0; i < 3; i++) {
            double angle = (i * 2.0D * Math.PI / 3.0D) + (random.nextDouble() * 0.2D);
            double dist = 6.0D;
            double ox = this.getX() + Math.cos(angle) * dist;
            double oy = this.getY();
            double oz = this.getZ() + Math.sin(angle) * dist;

            DendroRevivalPithEntity pith = new DendroRevivalPithEntity(com.soybe.awakeningsystem.entity.ModEntities.DENDRO_REVIVAL_PITH.get(), sl);
            pith.setPos(ox, oy, oz);
            sl.addFreshEntity(pith);
            this.activePiths.add(pith);
        }

        for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
            player.displayClientMessage(Component.translatable("boss.awakening_system.dendro_hypostasis.crisis"), true);
        }
    }

    @Override
    protected boolean checkRevivalObjectsCleared(ServerLevel sl) {
        this.activePiths.removeIf(pith -> !pith.isAlive() || pith.isRemoved());
        return this.activePiths.isEmpty();
    }

    @Override
    protected void cleanupRevivalObjects(ServerLevel sl) {
        for (DendroRevivalPithEntity pith : this.activePiths) {
            if (pith.isAlive()) {
                pith.discard();
            }
        }
        this.activePiths.clear();
    }

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
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.dendro_hypostasis.defeated_teyvat"), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                    }
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.dendro_hypostasis.defeated_vanilla"), false);
                }
            }
        }

        if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.PRIMOGEM.get(), 25)));
        }
        sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2)));
        if (SystemItems.NAGADUS_EMERALD_GEMSTONE.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.NAGADUS_EMERALD_GEMSTONE.get(), 1)));
        }
        if (SystemItems.VISION_DENDRO.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.VISION_DENDRO.get(), 1)));
        }
    }

    @Override
    protected void handleClientParticles() {
        if (random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    this.getX() + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getY() + 1.0D + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 1.5D,
                    0, 0.05D, 0);
        }
    }
}
