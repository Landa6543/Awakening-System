package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Titán Colosal de Sculk (Colossal Sculk Warden).
 * Variante titánica, modificada y monumental del Warden para el Piso 10 de la Gran Torre.
 * Mide más de 6 bloques de altura, posee 1200 HP, barra de jefe dedicada y ataques catastróficos.
 */
public class ColossalSculkWardenEntity extends Warden {

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("Titán Colosal de Sculk").withStyle(net.minecraft.ChatFormatting.DARK_AQUA, net.minecraft.ChatFormatting.BOLD),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_20
    ).setDarkenScreen(true);

    private int slamCooldown = 180;
    private int barrageCooldown = 100;
    private int ambientDarknessTimer = 0;
    private boolean enraged = false;

    public ColossalSculkWardenEntity(EntityType<? extends Warden> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
        this.setCustomName(Component.literal("Titán Colosal de Sculk").withStyle(net.minecraft.ChatFormatting.DARK_AQUA, net.minecraft.ChatFormatting.BOLD));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Warden.createAttributes()
                .add(Attributes.MAX_HEALTH, 1200.0D)
                .add(Attributes.ATTACK_DAMAGE, 45.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (player.level() == this.level()) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setPose(Pose pose) {
        // Bloquear completamente la pose de excavar (DIGGING) o emerger para que el jefe siempre esté de pie
        if (pose == Pose.DIGGING || pose == Pose.EMERGING) {
            super.setPose(Pose.STANDING);
            return;
        }
        super.setPose(pose);
    }

    @Override
    public void remove(RemovalReason reason) {
        // Bloquear descarte espurio por IA de excavación de Warden si sigue con vida
        if (reason == RemovalReason.DISCARDED && this.isAlive() && !this.level().isClientSide) {
            return;
        }
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
        this.bossEvent.setVisible(false);
    }

    public boolean isRoaring() {
        return this.getPose() == Pose.ROARING;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
        return true;
    }

    @Override
    public void checkDespawn() {
        // Los jefes de torre nunca despawnean
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // 1. Desactivar permanentemente la rutina de excavar del Brain de Warden
            this.getBrain().setMemoryWithExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.DIG_COOLDOWN, net.minecraft.util.Unit.INSTANCE, 1200000L);

            // 2. Sincronizar barra de salud del jefe
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            // 3. Adquisición proactiva de objetivo en la arena (hasta 54 bloques)
            LivingEntity curTarget = this.getTarget();
            if (curTarget == null || !curTarget.isAlive() || this.distanceToSqr(curTarget) > 2916.0D) {
                Player nearest = this.level().getNearestPlayer(this, 54.0D);
                if (nearest != null && !nearest.isCreative() && !nearest.isSpectator()) {
                    this.setTarget(nearest);
                    this.increaseAngerAt(nearest, 100, false);
                }
            }

            // Detección de fase de Enfurecimiento (< 50% de vida)
            if (!this.enraged && this.getHealth() <= this.getMaxHealth() * 0.5F) {
                this.enraged = true;
                this.bossEvent.setColor(BossEvent.BossBarColor.RED);
                this.bossEvent.setName(Component.literal("Titán Colosal de Sculk (Enfurecido)").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD));

                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 3.5F, 0.6F);
                    sl.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 3.0D, this.getZ(), 80, 2.0D, 1.5D, 2.0D, 0.2D);
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 2.0D, this.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);

                    // Convocar 2 guardianes Golems de Sculk despiertos
                    for (int g = 0; g < 2; g++) {
                        double offX = (g == 0 ? -4.0D : 4.0D);
                        SculkGolemEntity minion = new SculkGolemEntity(ModEntities.SCULK_GOLEM.get(), sl);
                        minion.moveTo(this.getX() + offX, this.getY(), this.getZ() + 2.0D, 0, 0);
                        minion.getEntityData().set(SculkGolemEntity.IS_STATUE, false);
                        LivingEntity t = this.getTarget();
                        if (t != null) minion.setTarget(t);
                        sl.addFreshEntity(minion);
                    }
                }

                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, false));
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 0, false, false));
            }

            // Aura constante de oscuridad en el Piso 10
            ambientDarknessTimer++;
            if (ambientDarknessTimer >= 100) {
                ambientDarknessTimer = 0;
                List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(28.0D));
                for (Player p : nearbyPlayers) {
                    if (!p.isCreative() && !p.isSpectator()) {
                        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 140, 0, false, false, true));
                    }
                }
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double dist = this.distanceTo(target);

                // 1. Aplastamiento Sísmico Colosal (Slam a corta/media distancia)
                if (--slamCooldown <= 0 && dist <= 16.0D) {
                    triggerColossalSlam();
                    slamCooldown = (this.enraged ? 130 : 200) + this.random.nextInt(40);
                }

                // 2. Ráfaga Sónica Continua a larga distancia
                if (--barrageCooldown <= 0 && dist > 5.0D && dist <= 32.0D) {
                    triggerSonicBarrage(target);
                    barrageCooldown = (this.enraged ? 80 : 130) + this.random.nextInt(30);
                }
            }
        }
    }

    /**
     * Aplastamiento monumental en el suelo: libera un anillo de ondas sónicas y almas de sculk
     * que lanzan por los aires a los jugadores cercanos y destrozan defensas.
     */
    private void triggerColossalSlam() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        this.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        this.playSound(SoundEvents.WARDEN_ROAR, 2.5F, 0.65F);
        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 2.0F, 0.75F);

        // Ondas de partículas en anillo
        double px = this.getX();
        double py = this.getY();
        double pz = this.getZ();

        for (int i = 0; i < 36; i++) {
            double angle = (i * 10.0D) * Math.PI / 180.0D;
            double dx = Math.cos(angle) * 7.0D;
            double dz = Math.sin(angle) * 7.0D;
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, px + dx, py + 1.0D, pz + dz, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, px + dx * 0.5D, py + 0.5D, pz + dz * 0.5D, 8, 0.5D, 0.5D, 0.5D, 0.05D);
        }

        AABB slamArea = this.getBoundingBox().inflate(14.0D, 4.0D, 14.0D);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, slamArea,
                e -> e != this && e.isAlive() && !(e instanceof SculkGolemEntity));

        for (LivingEntity e : targets) {
            e.hurt(this.damageSources().mobAttack(this), 30.0F);
            Vec3 knockback = e.position().subtract(this.position()).normalize().scale(1.8D);
            e.push(knockback.x, 0.65D, knockback.z);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2, false, true));
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, true));
        }
    }

    /**
     * Ráfaga de cañón sónico directo hacia el objetivo.
     */
    private void triggerSonicBarrage(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 2.2F, 0.85F);
        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 2.5F, 0.8F);

        Vec3 from = this.position().add(0, 4.5D, 0);
        Vec3 to = target.getEyePosition();
        Vec3 dir = to.subtract(from).normalize();

        // Trazar el haz sónico
        double totalDist = from.distanceTo(to);
        for (double d = 1.0D; d < totalDist; d += 1.2D) {
            Vec3 p = from.add(dir.scale(d));
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }

        target.hurt(this.damageSources().sonicBoom(this), 35.0F);
        Vec3 pushDir = dir.scale(1.5D);
        target.push(pushDir.x, 0.35D, pushDir.z);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 2.0F, 0.75F);
            Vec3 knock = living.position().subtract(this.position()).normalize().scale(1.6D);
            living.push(knock.x, 0.45D, knock.z);
            living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 140, 0));
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            super.handleEntityEvent(id);
            this.swing(net.minecraft.world.InteractionHand.MAIN_HAND, false);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.bossEvent.removeAllPlayers();
        this.bossEvent.setVisible(false);

        if (this.level() instanceof ServerLevel sl) {
            com.soybe.awakeningsystem.DimensionGenSavedData.get(sl.getServer()).setTowerFloor10BossDefeated(true);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 2.0F, 0.9F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 3.0D, this.getZ(), 4, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 2.0D, this.getZ(), 100, 2.0D, 2.0D, 2.0D, 0.15D);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        // Botín Monumental de Cristales de Maná Míticos (Rango S) y Legendarios (Rango A)
        int shardSCount = 15 + this.random.nextInt(11);
        int clusterSCount = 2 + this.random.nextInt(3);
        this.spawnAtLocation(new ItemStack(SystemItems.MANA_CRYSTAL_SHARD_S.get(), shardSCount));
        this.spawnAtLocation(new ItemStack(SystemItems.MANA_CRYSTAL_CLUSTER_S_ITEM.get(), clusterSCount));

        int shardACount = 25 + this.random.nextInt(16);
        int clusterACount = 4 + this.random.nextInt(4);
        this.spawnAtLocation(new ItemStack(SystemItems.MANA_CRYSTAL_SHARD_A.get(), shardACount));
        this.spawnAtLocation(new ItemStack(SystemItems.MANA_CRYSTAL_CLUSTER_A_ITEM.get(), clusterACount));

        // Recompensas de Sculk y Arcanas
        this.spawnAtLocation(new ItemStack(Items.ECHO_SHARD, 8));
        this.spawnAtLocation(new ItemStack(Blocks.SCULK_CATALYST, 3));
        this.spawnAtLocation(new ItemStack(Blocks.SCULK_SHRIEKER, 2));
        this.spawnAtLocation(new ItemStack(Items.NETHERITE_SCRAP, 4));
        this.spawnAtLocation(new ItemStack(Items.NETHER_STAR, 1));
    }
}
