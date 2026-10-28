package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Reina Araña Madre (Mother Spider Boss) - Variante Jefe.
 * Versión colosal y aterradora con escala multiplicada (más de 2.5 bloques de altura y 4 de ancho).
 * Posee barra de jefe dedicada, 450 HP, disparo de múltiples telarañas simultáneas,
 * invocación de crías arácnidas tóxicas y pisotones sísmicos de área.
 */
public class MotherSpiderBossEntity extends MotherSpiderEntity {

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("entity.awakening_system.mother_spider_boss"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(true);

    private boolean summonedPhase1 = false;
    private boolean summonedPhase2 = false;
    private boolean summonedPhase3 = false;
    private int groundSlamCooldown = 100;

    public MotherSpiderBossEntity(EntityType<? extends MotherSpiderBossEntity> type, Level level) {
        super(type, level);
        this.setBoss(true);
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 450.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.80D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    public float getSpiderScale() {
        return 2.5F;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            float healthPct = this.getHealth() / this.getMaxHealth();

            // Invocaciones de crías según umbral de vida
            if (healthPct <= 0.75F && !this.summonedPhase1) {
                this.summonedPhase1 = true;
                summonBrood(2);
            }
            if (healthPct <= 0.50F && !this.summonedPhase2) {
                this.summonedPhase2 = true;
                summonBrood(3);
            }
            if (healthPct <= 0.25F && !this.summonedPhase3) {
                this.summonedPhase3 = true;
                summonBrood(4);
            }

            // Pisotón sísmico si hay jugadores cerca
            if (--groundSlamCooldown <= 0) {
                groundSlamCooldown = 120 + this.random.nextInt(60);
                performGroundQuake();
            }
        }
    }

    @Override
    protected void performWebShot() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.33D) - this.getY(1.8D);
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        // Disparo en abanico de 3 telarañas colosales
        double[] angles = {-0.25D, 0.0D, 0.25D};
        for (double angleOffset : angles) {
            double dirX = dx * Math.cos(angleOffset) - dz * Math.sin(angleOffset);
            double dirZ = dx * Math.sin(angleOffset) + dz * Math.cos(angleOffset);

            SpiderWebProjectileEntity web = new SpiderWebProjectileEntity(this.level(), this, 14.0F, true);
            web.setPos(this.getX(), this.getY(1.8D), this.getZ());
            web.shoot(dirX, dy + dist * 0.12D, dirZ, 1.5F, 1.5F);
            this.level().addFreshEntity(web);
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.MOTHER_SPIDER_SHOOT_WEB.get(), SoundSource.HOSTILE, 2.2F, 0.75F);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return ModSounds.MOTHER_SPIDER_BOSS_AMBIENT.get();
    }

    @Override
    public float getVoicePitch() {
        return 0.70F + this.random.nextFloat() * 0.10F;
    }

    private void performGroundQuake() {
        AABB area = this.getBoundingBox().inflate(5.0D, 2.0D, 5.0D);
        List<Player> nearby = this.level().getEntitiesOfClass(Player.class, area);

        if (!nearby.isEmpty()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.MOTHER_SPIDER_ATTACK.get(), SoundSource.HOSTILE, 2.4F, 0.65F);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        this.getX(), this.getY(), this.getZ(), 4, 1.5D, 0.2D, 1.5D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                        this.getX(), this.getY() + 0.5D, this.getZ(), 30, 2.0D, 0.5D, 2.0D, 0.1D);
            }

            for (Player player : nearby) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.hurt(this.damageSources().mobAttack(this), 10.0F);
                    Vec3 push = player.position().subtract(this.position()).normalize().scale(1.2D);
                    player.setDeltaMovement(push.x, 0.45D, push.z);
                    player.hurtMarked = true;
                }
            }
        }
    }

    private void summonBrood(int count) {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.MOTHER_SPIDER_ATTACK.get(), SoundSource.HOSTILE, 2.2F, 0.70F);

            for (int i = 0; i < count; i++) {
                CaveSpider spiderling = EntityType.CAVE_SPIDER.create(serverLevel);
                if (spiderling != null) {
                    double ox = (this.random.nextDouble() - 0.5D) * 3.0D;
                    double oz = (this.random.nextDouble() - 0.5D) * 3.0D;
                    spiderling.moveTo(this.getX() + ox, this.getY() + 0.5D, this.getZ() + oz, this.getYRot(), 0.0F);
                    spiderling.setTarget(this.getTarget());
                    serverLevel.addFreshEntity(spiderling);

                    serverLevel.sendParticles(ParticleTypes.WITCH,
                            spiderling.getX(), spiderling.getY() + 0.3D, spiderling.getZ(),
                            10, 0.3D, 0.3D, 0.3D, 0.0D);
                }
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("SummonedPhase1", this.summonedPhase1);
        compound.putBoolean("SummonedPhase2", this.summonedPhase2);
        compound.putBoolean("SummonedPhase3", this.summonedPhase3);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SummonedPhase1")) this.summonedPhase1 = compound.getBoolean("SummonedPhase1");
        if (compound.contains("SummonedPhase2")) this.summonedPhase2 = compound.getBoolean("SummonedPhase2");
        if (compound.contains("SummonedPhase3")) this.summonedPhase3 = compound.getBoolean("SummonedPhase3");
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        // Botín colosal de jefe
        this.spawnAtLocation(new ItemStack(Items.STRING, 12 + this.random.nextInt(12)));
        this.spawnAtLocation(new ItemStack(Items.SPIDER_EYE, 6 + this.random.nextInt(6)));
        this.spawnAtLocation(new ItemStack(Items.COBWEB, 3 + this.random.nextInt(4)));
        // Monedas de Awakening de Jefe
        this.spawnAtLocation(SystemItems.coin(20 + this.random.nextInt(15)));

        if (this.random.nextFloat() < 0.50F && SystemItems.DUNGEON_KEY != null && SystemItems.DUNGEON_KEY.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.DUNGEON_KEY.value(), 1));
        }
    }
}
