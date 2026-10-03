package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.artifact.GenshinArtifactItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Jefe Legendario de Teyvat: Rey Acechador Dorado (Golden Wolflord)
 * Monarca de los Sabuesos del Acecho de la Isla Tsurumi / Enkanomiya.
 * - Bestia voladora colosal de las fallas del Abismo.
 * - Inflige el estado de Corrosión que atraviesa escudos y armaduras.
 * - Escudo Geo en la segunda fase.
 */
public class GoldenWolflordBossEntity extends Monster {

    public static final EntityDataAccessor<Boolean> DATA_SHIELD_ACTIVE =
            SynchedEntityData.defineId(GoldenWolflordBossEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("§6§lRey Acechador Dorado §7- §eMonarca del Abismo"),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int attackCooldown = 60;
    private int diveTicks = 0;
    private Vec3 diveTarget = null;

    public GoldenWolflordBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 450;
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
        this.setCustomName(Component.literal("§6§lRey Acechador Dorado"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FLYING_SPEED, 0.40D)
                .add(Attributes.ATTACK_DAMAGE, 28.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SHIELD_ACTIVE, false);
    }

    public boolean isShieldActive() {
        return this.entityData.get(DATA_SHIELD_ACTIVE);
    }

    public void setShieldActive(boolean active) {
        this.entityData.set(DATA_SHIELD_ACTIVE, active);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WolflordAttackGoal(this));
        this.goalSelector.addGoal(2, new WolflordCircleHoverGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isShieldActive()) {
            amount *= 0.30F; // 70% de reducción de daño bajo escudo Geo
        }

        boolean result = super.hurt(source, amount);

        if (result && !isShieldActive() && this.getHealth() <= this.getMaxHealth() * 0.50F) {
            setShieldActive(true);
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.HOSTILE, 1.5F, 0.6F);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.5D, this.getZ(), 50, 2.0D, 1.0D, 2.0D, 0.1D);
            }
        }
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            // Si está activo el escudo, emitir partículas de Geo brillantes
            if (isShieldActive() && this.tickCount % 5 == 0 && this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.WAX_ON, this.getX(), this.getY() + 1.5D, this.getZ(), 8, 1.5D, 1.0D, 1.5D, 0.05D);
            }

            // Manejo de la embestida en picada
            if (diveTicks > 0) {
                diveTicks--;
                if (diveTarget != null) {
                    Vec3 dir = diveTarget.subtract(this.position()).normalize().scale(1.1D);
                    this.setDeltaMovement(dir);

                    // Dañar entidades en el trayecto
                    AABB box = this.getBoundingBox().inflate(1.5D);
                    for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
                        if (target != this && !(target instanceof GoldenWolflordBossEntity)) {
                            target.hurt(this.damageSources().mobAttack(this), 22.0F);
                            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.6D, 0));
                        }
                    }
                }
            }
        }
    }

    public void triggerDiveAttack(Vec3 targetPos) {
        this.diveTarget = targetPos;
        this.diveTicks = 35;
        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.8F, 0.8F);
    }

    public void shootCorrosionSkulls(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.5F, 0.9F);

        for (int i = 0; i < 3; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * 3.0D;
            double oy = (this.random.nextDouble() - 0.5D) * 1.5D;
            double oz = (this.random.nextDouble() - 0.5D) * 3.0D;

            Vec3 shootVec = target.position().add(0, 0.8D, 0).subtract(this.position().add(ox, oy + 1.5D, oz)).normalize();

            WitherSkull skull = new WitherSkull(this.level(), this, shootVec);
            skull.setPos(this.getX() + ox, this.getY() + 1.5D + oy, this.getZ() + oz);
            this.level().addFreshEntity(skull);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            // Recompensas legendarias del Wolflord
            if (SystemItems.RIFTBORN_REGALIA.isBound()) {
                dropItem(new ItemStack(SystemItems.RIFTBORN_REGALIA.get(), 2 + this.random.nextInt(3)));
            }
            if (SystemItems.GOLDEN_WOLFLORD_CORE.isBound()) {
                dropItem(new ItemStack(SystemItems.GOLDEN_WOLFLORD_CORE.get(), 1));
            }
            boolean isTeyvat = this.level().dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                dropItem(new ItemStack(SystemItems.PRIMOGEM.get(), 45 + this.random.nextInt(25)));
            }
            if (isTeyvat && SystemItems.MORA.isBound()) {
                dropItem(new ItemStack(SystemItems.MORA.get(), 64));
            } else if (!isTeyvat && SystemItems.AWAKENING_COIN.isBound()) {
                dropItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 25));
            }

            // Artefacto legendario 5 estrellas
            try {
                var items = net.minecraft.core.registries.BuiltInRegistries.ITEM;
                java.util.List<GenshinArtifactItem> arts = new java.util.ArrayList<>();
                for (var it : items) {
                    if (it instanceof GenshinArtifactItem art) {
                        arts.add(art);
                    }
                }
                if (!arts.isEmpty()) {
                    dropItem(new ItemStack(arts.get(this.random.nextInt(arts.size()))));
                }
            } catch (Exception ignored) {}
        }
    }

    private void dropItem(ItemStack stack) {
        ItemEntity it = new ItemEntity(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), stack);
        it.setDefaultPickUpDelay();
        this.level().addFreshEntity(it);
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

    // --- METAS DE IA PARA VUELO Y COMBATE ---
    static class WolflordCircleHoverGoal extends Goal {
        private final GoldenWolflordBossEntity boss;
        private float angle = 0.0F;

        public WolflordCircleHoverGoal(GoldenWolflordBossEntity boss) {
            this.boss = boss;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.boss.diveTicks <= 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.boss.getTarget();
            Vec3 center = (target != null) ? target.position() : this.boss.position();

            angle += 0.04F;
            double radius = 16.0D;
            double targetX = center.x + Math.cos(angle) * radius;
            double targetZ = center.z + Math.sin(angle) * radius;
            double targetY = center.y + 12.0D + Math.sin(angle * 0.5D) * 3.0D;

            Vec3 moveVec = new Vec3(targetX - this.boss.getX(), targetY - this.boss.getY(), targetZ - this.boss.getZ()).normalize().scale(0.35D);
            this.boss.setDeltaMovement(this.boss.getDeltaMovement().scale(0.7D).add(moveVec));
        }
    }

    static class WolflordAttackGoal extends Goal {
        private final GoldenWolflordBossEntity boss;

        public WolflordAttackGoal(GoldenWolflordBossEntity boss) {
            this.boss = boss;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.boss.getTarget() != null && this.boss.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.boss.getTarget();
            if (target == null) return;

            this.boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (this.boss.attackCooldown > 0) {
                this.boss.attackCooldown--;
            } else {
                this.boss.attackCooldown = 80 + this.boss.random.nextInt(40);
                if (this.boss.random.nextBoolean()) {
                    // Ataque en picada
                    this.boss.triggerDiveAttack(target.position());
                } else {
                    // Ráfaga de calaveras de corrosión
                    this.boss.shootCorrosionSkulls(target);
                }
            }
        }
    }
}
