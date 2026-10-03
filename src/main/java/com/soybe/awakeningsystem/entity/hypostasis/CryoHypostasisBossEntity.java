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

public class CryoHypostasisBossEntity extends HypostasisBaseBossEntity {

    public CryoHypostasisBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(getBossTitle());
        this.setCustomNameVisible(true);
    }

    @Override
    protected Component getBossTitle() {
        return Component.literal("§b✦ Daleth — Hipóstasis Cryo ✦");
    }

    @Override
    protected BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.BLUE;
    }

    @Override
    public ParticleOptions getElementalParticle() {
        return ParticleTypes.SNOWFLAKE;
    }

    @Override
    public int getElementalColorHex() {
        return 0x88EEFF; // Azul Glacial / Hielo Cryo
    }

    @Override
    protected void executeAttackStep(ServerLevel sl, Player target, HypostasisAttackType attack, float progress) {
        Vec3 toTarget = target.position().subtract(this.position()).normalize();

        switch (attack) {
            case CLAP -> {
                if (progress >= 0.58F && progress <= 0.65F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.8F, 1.4F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.HOSTILE, 1.5F, 0.8F);

                    Vec3 strikePos = this.position().add(toTarget.scale(2.6D));
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, strikePos.x, strikePos.y, strikePos.z, 60, 1.2D, 1.0D, 1.2D, 0.15D);
                    sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, strikePos.x, strikePos.y, strikePos.z, 25, 0.8D, 0.8D, 0.8D, 0.2D);

                    AABB cone = new AABB(strikePos.x - 3.8D, strikePos.y - 1.5D, strikePos.z - 3.8D,
                            strikePos.x + 3.8D, strikePos.y + 3.0D, strikePos.z + 3.8D);

                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, cone)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().freeze(), 28.0F);
                            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                            nearby.setTicksFrozen(200);
                        }
                    }
                }
            }
            case MISSILES -> {
                int step = (int) (progress * 10);
                if (progress > 0.25F && progress < 0.85F && (step % 2 == 0)) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.HOSTILE, 0.8F, 1.5F);
                    Vec3 targetArea = target.position().add((rng.nextDouble() - 0.5D) * 2.0D, 0, (rng.nextDouble() - 0.5D) * 2.0D);
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, targetArea.x, targetArea.y + 0.2D, targetArea.z, 15, 0.4D, 0.2D, 0.4D, 0.05D);

                    AABB hitBox = new AABB(targetArea.x - 1.2D, targetArea.y - 0.5D, targetArea.z - 1.2D,
                            targetArea.x + 1.2D, targetArea.y + 2.0D, targetArea.z + 1.2D);

                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, hitBox)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().freeze(), 12.0F);
                            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                        }
                    }
                }
            }
            case SPECIAL_ACTION, BURST_ULTIMATE -> {
                if (progress >= 0.40F && progress <= 0.45F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.POWDER_SNOW_BREAK, SoundSource.HOSTILE, 1.5F, 0.8F);
                    double radius = 7.0D;
                    for (int i = 0; i < 36; i++) {
                        double angle = Math.toRadians(i * 10);
                        double px = this.getX() + Math.cos(angle) * radius;
                        double pz = this.getZ() + Math.sin(angle) * radius;
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, px, this.getY() + 0.5D, pz, 4, 0.1D, 0.1D, 0.1D, 0.05D);
                    }
                    AABB waveBox = this.getBoundingBox().inflate(radius, 2.0D, radius);
                    for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, waveBox)) {
                        if (nearby != this) {
                            nearby.hurt(nearby.damageSources().freeze(), 20.0F);
                            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                        }
                    }
                }
            }
            default -> {}
        }
    }

    @Override
    protected void spawnRevivalObjects(ServerLevel sl) {
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0F, 0.5F);
        for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
            player.displayClientMessage(Component.literal("§b§l✦ [DALETH EN CRISIS] §f¡Aplica ataques de Pyro para derretir la coraza helada antes de que restaure su vitalidad!"), true);
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
                    sp.displayClientMessage(Component.literal("§b§l✦ ¡HIPÓSTASIS CRYO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e."), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                    }
                    sp.displayClientMessage(Component.literal("§b§l✦ ¡HIPÓSTASIS CRYO DERROTADO! §6+24 Monedas de Despertar§e."), false);
                }
            }
        }

        if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.PRIMOGEM.get(), 25)));
        }
        sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2)));
        if (SystemItems.CRYSTALLINE_BLOOM.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.CRYSTALLINE_BLOOM.get(), 2 + random.nextInt(2))));
        }
        if (SystemItems.CRYO_HYPOSTASIS_BLOCK.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.CRYO_HYPOSTASIS_BLOCK.get(), 1)));
        }
        if (SystemItems.SHIVADA_JADE_GEMSTONE.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.SHIVADA_JADE_GEMSTONE.get(), 1)));
        }
        if (SystemItems.VISION_CRYO.isBound()) {
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), new ItemStack(SystemItems.VISION_CRYO.get(), 1)));
        }
    }

    @Override
    protected void handleClientParticles() {
        if (random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE,
                    this.getX() + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getY() + 1.0D + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 1.5D,
                    0, 0.03D, 0);
        }
    }
}
