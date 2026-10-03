package com.soybe.awakeningsystem.entity.hypostasis;

import com.soybe.awakeningsystem.ModSounds;
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

public class AnemoHypostasisBossEntity extends HypostasisBaseBossEntity {

    private final List<AnemoRevivalOrbEntity> activeOrbs = new ArrayList<>();

    public AnemoHypostasisBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(getBossTitle());
        this.setCustomNameVisible(true);
    }

    @Override
    protected Component getBossTitle() {
        return Component.literal("§b✦ Beth — Hipóstasis Anemo ✦");
    }

    @Override
    protected BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.GREEN;
    }

    @Override
    public ParticleOptions getElementalParticle() {
        return ParticleTypes.CLOUD;
    }

    @Override
    public int getElementalColorHex() {
        return 0x00FFCC; // Cyan / Turquesa Anemo
    }

    @Override
    protected void executeAttackStep(ServerLevel sl, Player target, HypostasisAttackType attack, float progress) {
        Vec3 toTarget = target.position().subtract(this.position()).normalize();

        switch (attack) {
            case CLAP -> {
                // Clap ocurre en el 60% del progreso con ráfaga nítida de viento y onda de choque
                if (progress >= 0.58F && progress <= 0.65F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 1.8F, 1.2F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.5F, 0.9F);

                    // Onda de choque frontal de viento
                    Vec3 strikePos = this.position().add(toTarget.scale(2.5D));
                    sl.sendParticles(ParticleTypes.EXPLOSION, strikePos.x, strikePos.y, strikePos.z, 2, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, strikePos.x, strikePos.y, strikePos.z, 5, 0.5D, 0.5D, 0.5D, 0.05D);
                    sl.sendParticles(ParticleTypes.CLOUD, strikePos.x, strikePos.y, strikePos.z, 50, 1.5D, 0.8D, 1.5D, 0.15D);

                    AABB cone = new AABB(strikePos.x - 4.0D, strikePos.y - 2.0D, strikePos.z - 4.0D,
                            strikePos.x + 4.0D, strikePos.y + 3.0D, strikePos.z + 4.0D);

                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, cone)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().mobAttack(this), 28.0F);
                            nearby.setDeltaMovement(toTarget.x * 1.5D, 0.95D, toTarget.z * 1.5D);
                        }
                    }
                }
            }
            case MISSILES -> {
                if (progress >= 0.20F && progress <= 0.25F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BREEZE_SHOOT, SoundSource.HOSTILE, 1.5F, 1.2F);
                }

                // Dispara proyectiles en ráfagas (cada 15% de progreso)
                int step = (int) (progress * 10);
                if (progress > 0.25F && progress < 0.85F && (step % 2 == 0)) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 0.6F, 1.6F);
                    Vec3 shotPos = this.position().add(0, 1.0D, 0);
                    sl.sendParticles(ParticleTypes.CLOUD, shotPos.x, shotPos.y, shotPos.z, 10, 0.2D, 0.2D, 0.2D, 0.05D);

                    // Impacto guiado sobre el área del jugador
                    Vec3 targetArea = target.position().add((rng.nextDouble() - 0.5D) * 2.0D, 0, (rng.nextDouble() - 0.5D) * 2.0D);
                    sl.sendParticles(ParticleTypes.CLOUD, targetArea.x, targetArea.y + 0.5D, targetArea.z, 8, 0.3D, 0.3D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, targetArea.x, targetArea.y + 0.5D, targetArea.z, 1, 0, 0, 0, 0);

                    if (target.position().distanceToSqr(targetArea) < 4.0D) {
                        target.hurt(target.damageSources().mobAttack(this), 10.0F);
                        target.setDeltaMovement(target.getDeltaMovement().add(0, 0.35D, 0));
                    }
                }
            }
            case SPECIAL_ACTION -> {
                // Invocación de 3 Tornados de Vórtice
                if (progress >= 0.38F && progress <= 0.44F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 1.8F, 0.85F);

                    for (int i = 0; i < 3; i++) {
                        double angle = (2 * Math.PI / 3) * i;
                        double tx = target.getX() + Math.cos(angle) * 4.5D;
                        double tz = target.getZ() + Math.sin(angle) * 4.5D;
                        sl.sendParticles(ParticleTypes.CLOUD, tx, target.getY() + 1.0D, tz, 60, 0.8D, 2.5D, 0.8D, 0.15D);
                        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, tx, target.getY() + 1.0D, tz, 4, 0.4D, 0.8D, 0.4D, 0.05D);
                    }

                    AABB vortexArea = target.getBoundingBox().inflate(5.0D, 2.0D, 5.0D);
                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, vortexArea)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().mobAttack(this), 22.0F);
                            nearby.setDeltaMovement(0, 1.25D, 0);
                            nearby.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1));
                        }
                    }
                }
            }
            case BURST_ULTIMATE -> {
                // Alas de Mariposa Anemo: Succión por vacío y Estallido final
                if (progress >= 0.05F && progress <= 0.12F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BREEZE_INHALE, SoundSource.HOSTILE, 1.6F, 0.9F);
                }

                if (progress < 0.75F) {
                    // Atracción por vacío hacia el núcleo
                    Vec3 pull = this.position().subtract(target.position()).normalize().scale(0.12D);
                    target.setDeltaMovement(target.getDeltaMovement().add(pull));
                    sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0D, this.getZ(), 8, 1.5D, 1.5D, 1.5D, -0.2D);
                } else if (progress >= 0.75F && progress <= 0.82F) {
                    // ¡Estallido y aleteo de mariposa auténtico y nítido!
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.2F, 1.4F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 1.8F, 1.0F);

                    sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.0D, this.getZ(), 1, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0D, this.getZ(), 140, 4.0D, 2.0D, 4.0D, 0.3D);

                    AABB blastArea = this.getBoundingBox().inflate(9.0D, 4.0D, 9.0D);
                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, blastArea)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().mobAttack(this), 42.0F);
                            Vec3 push = nearby.position().subtract(this.position()).normalize().scale(1.8D);
                            nearby.setDeltaMovement(push.x, 0.9D, push.z);
                        }
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
            double ox = this.getX() + Math.cos(angle) * 7.0D;
            double oz = this.getZ() + Math.sin(angle) * 7.0D;
            double oy = this.getY() + 1.0D;

            AnemoRevivalOrbEntity orb = new AnemoRevivalOrbEntity(com.soybe.awakeningsystem.entity.ModEntities.ANEMO_REVIVAL_ORB.get(), sl);
            orb.setPos(ox, oy, oz);
            sl.addFreshEntity(orb);
            this.activeOrbs.add(orb);
        }

        for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
            player.displayClientMessage(Component.literal("§b§l✦ [BETH EN CRISIS] §f¡Recoge o destruye las §b3 Esferas de Viento §fantes de que regenere su núcleo!"), true);
        }
    }

    @Override
    protected boolean checkRevivalObjectsCleared(ServerLevel sl) {
        this.activeOrbs.removeIf(orb -> !orb.isAlive() || orb.isRemoved());
        return this.activeOrbs.isEmpty();
    }

    @Override
    protected void cleanupRevivalObjects(ServerLevel sl) {
        for (AnemoRevivalOrbEntity orb : this.activeOrbs) {
            if (orb.isAlive()) {
                orb.discard();
            }
        }
        this.activeOrbs.clear();
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
                    sp.displayClientMessage(Component.literal("§b§l✦ ¡HIPÓSTASIS ANEMO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e."), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                    }
                    sp.displayClientMessage(Component.literal("§b§l✦ ¡HIPÓSTASIS ANEMO DERROTADO! §6+24 Monedas de Despertar§e."), false);
                }
            }
        }

        // Drops físicos: Cristales de Turquesa Vayuda, Destinos y Artefactos
        if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.PRIMOGEM.get(), 25)));
        }
        sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2)));
        if (SystemItems.HURRICANE_SEED.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.HURRICANE_SEED.get(), 2 + random.nextInt(2))));
        }
        if (SystemItems.ANEMO_HYPOSTASIS_BLOCK.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.ANEMO_HYPOSTASIS_BLOCK.get(), 1)));
        }
        if (SystemItems.VISION_ANEMO.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.VISION_ANEMO.get(), 1)));
        }
    }

    @Override
    protected void handleClientParticles() {
        if (random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.CLOUD,
                    this.getX() + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getY() + 1.0D + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 1.5D,
                    0, 0.05D, 0);
        }
    }
}
