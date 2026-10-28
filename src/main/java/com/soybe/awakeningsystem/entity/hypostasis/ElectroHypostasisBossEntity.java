package com.soybe.awakeningsystem.entity.hypostasis;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.LightningBolt;
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

public class ElectroHypostasisBossEntity extends HypostasisBaseBossEntity {

    private final List<ElectroRevivalPrismEntity> activePrisms = new ArrayList<>();

    public ElectroHypostasisBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(getBossTitle());
        this.setCustomNameVisible(true);
    }

    @Override
    protected Component getBossTitle() {
        return Component.translatable("boss.awakening_system.hypostasis.electro");
    }

    @Override
    protected BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.PURPLE;
    }

    @Override
    public ParticleOptions getElementalParticle() {
        return ParticleTypes.ELECTRIC_SPARK;
    }

    @Override
    public int getElementalColorHex() {
        return 0xBF00FF;
    }

    @Override
    protected void executeAttackStep(ServerLevel sl, Player target, HypostasisAttackType attack, float progress) {
        Vec3 toTarget = target.position().subtract(this.position()).normalize();

        switch (attack) {
            case CLAP -> {
                if (progress >= 0.58F && progress <= 0.65F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.2F, 1.4F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.5F, 1.2F);

                    Vec3 strikePos = this.position().add(toTarget.scale(2.8D));
                    sl.sendParticles(ParticleTypes.FLASH, strikePos.x, strikePos.y, strikePos.z, 1, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, strikePos.x, strikePos.y, strikePos.z, 60, 1.5D, 1.0D, 1.5D, 0.2D);

                    for (int i = 1; i <= 4; i++) {
                        Vec3 boltPos = this.position().add(toTarget.scale(i * 2.2D));
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                        if (bolt != null) {
                            bolt.moveTo(boltPos.x, boltPos.y, boltPos.z);
                            bolt.setVisualOnly(true);
                            sl.addFreshEntity(bolt);
                        }
                    }

                    AABB lineBox = new AABB(strikePos.x - 3.5D, strikePos.y - 1.5D, strikePos.z - 3.5D,
                            strikePos.x + 3.5D, strikePos.y + 3.0D, strikePos.z + 3.5D);

                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, lineBox)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().lightningBolt(), 32.0F);
                            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                        }
                    }
                }
            }
            case MISSILES -> {
                int step = (int) (progress * 12);
                if (progress > 0.20F && progress < 0.85F && (step % 2 == 0)) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE, 0.8F, 1.8F);
                    Vec3 shotPos = this.position().add(0, 1.2D, 0);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, shotPos.x, shotPos.y, shotPos.z, 12, 0.2D, 0.2D, 0.2D, 0.1D);

                    Vec3 targetArea = target.position().add((rng.nextDouble() - 0.5D) * 1.5D, 0.2D, (rng.nextDouble() - 0.5D) * 1.5D);
                    sl.sendParticles(ParticleTypes.PORTAL, targetArea.x, targetArea.y, targetArea.z, 15, 0.3D, 0.3D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, targetArea.x, targetArea.y, targetArea.z, 20, 0.4D, 0.4D, 0.4D, 0.1D);

                    if (target.position().distanceToSqr(targetArea) < 3.5D) {
                        target.hurt(target.damageSources().mobAttack(this), 12.0F);
                    }
                }
            }
            case SPECIAL_ACTION -> {
                if (progress >= 0.28F && progress <= 0.34F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.HOSTILE, 1.8F, 1.2F);
                }

                if (progress >= 0.30F && progress <= 0.70F) {
                    this.setDeltaMovement(toTarget.x * 0.95D, 0, toTarget.z * 0.95D);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.8D, this.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.1D);

                    AABB hitArea = this.getBoundingBox().inflate(1.2D);
                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, hitArea)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().mobAttack(this), 34.0F);
                            nearby.knockback(1.5D, -toTarget.x, -toTarget.z);
                            sl.playSound(null, nearby.getX(), nearby.getY(), nearby.getZ(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.2F, 1.4F);
                        }
                    }
                }
            }
            case BURST_ULTIMATE -> {
                if (progress >= 0.04F && progress <= 0.08F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.5F, 1.8F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.2F, 1.6F);
                }

                double rotAngle = progress * Math.PI * 4;

                for (int beam = 0; beam < 4; beam++) {
                    double beamAngle = rotAngle + (Math.PI / 2) * beam;
                    Vec3 beamDir = new Vec3(Math.cos(beamAngle), 0, Math.sin(beamAngle));

                    for (double d = 2.0D; d <= 9.0D; d += 1.0D) {
                        Vec3 beamPos = this.position().add(beamDir.scale(d)).add(0, 0.8D, 0);
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, beamPos.x, beamPos.y, beamPos.z, 4, 0.1D, 0.1D, 0.1D, 0.05D);
                        sl.sendParticles(ParticleTypes.PORTAL, beamPos.x, beamPos.y, beamPos.z, 2, 0.1D, 0.1D, 0.1D, 0.02D);
                    }

                    Vec3 checkPos = this.position().add(beamDir.scale(target.distanceTo(this))).add(0, 0.8D, 0);
                    if (target.distanceToSqr(this) >= 4.0D && target.distanceToSqr(this) <= 81.0D && target.position().distanceToSqr(checkPos) < 2.5D) {
                        target.hurt(target.damageSources().lightningBolt(), 15.0F);
                        target.knockback(0.4D, -beamDir.x, -beamDir.z);
                    }
                }
            }
        }
    }

    @Override
    protected void spawnRevivalObjects(ServerLevel sl) {
        cleanupRevivalObjects(sl);
        for (int i = 0; i < 3; i++) {
            double angle = (2 * Math.PI / 3) * i;
            double ox = this.getX() + Math.cos(angle) * 7.5D;
            double oz = this.getZ() + Math.sin(angle) * 7.5D;

            BlockPos below = BlockPos.containing(ox, this.getY(), oz);
            while (below.getY() > sl.getMinBuildHeight() && sl.getBlockState(below).isAir()) {
                below = below.below();
            }
            double oy = below.getY() + 1.2D;

            ElectroRevivalPrismEntity prism = new ElectroRevivalPrismEntity(com.soybe.awakeningsystem.entity.ModEntities.ELECTRO_REVIVAL_PRISM.get(), sl);
            prism.setPos(ox, oy, oz);
            sl.addFreshEntity(prism);
            this.activePrisms.add(prism);
        }

        for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
            player.displayClientMessage(Component.translatable("boss.awakening_system.electro_hypostasis.crisis"), true);
        }
    }

    @Override
    protected boolean checkRevivalObjectsCleared(ServerLevel sl) {
        this.activePrisms.removeIf(prism -> !prism.isAlive() || prism.isRemoved());
        return this.activePrisms.isEmpty();
    }

    @Override
    protected void cleanupRevivalObjects(ServerLevel sl) {
        for (ElectroRevivalPrismEntity prism : this.activePrisms) {
            if (prism.isAlive()) {
                prism.discard();
            }
        }
        this.activePrisms.clear();
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
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.electro_hypostasis.defeated_teyvat"), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                    }
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.electro_hypostasis.defeated_vanilla"), false);
                }
            }
        }

        if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.PRIMOGEM.get(), 25)));
        }
        sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2)));
        if (SystemItems.LIGHTNING_PRISM.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.LIGHTNING_PRISM.get(), 2 + random.nextInt(2))));
        }
        if (SystemItems.ELECTRO_HYPOSTASIS_BLOCK.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.ELECTRO_HYPOSTASIS_BLOCK.get(), 1)));
        }
        if (SystemItems.VISION_ELECTRO.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.VISION_ELECTRO.get(), 1)));
        }
    }

    @Override
    protected void handleClientParticles() {
        if (random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getY() + 1.0D + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 1.5D,
                    0, 0.05D, 0);
        }
    }
}
