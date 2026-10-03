package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Slime Elemental Canónico de Genshin Impact:
 * Criatura elástica con anatomía redondeada de gota de agua (Squash & Stretch),
 * rebotes parabólicos rítmicos fluidos y habilidades elementales de los 7 elementos de Teyvat.
 */
public class GenshinSlimeEntity extends Monster {

    public enum Element {
        PYRO(0, "pyro", "§cPyro", 0xFFE64A19, ParticleTypes.FLAME),
        HYDRO(1, "hydro", "§bHydro", 0xFF0288D1, ParticleTypes.SPLASH),
        ANEMO(2, "anemo", "§aAnemo", 0xFF00BFA5, ParticleTypes.CLOUD),
        ELECTRO(3, "electro", "§5Electro", 0xFF7B1FA2, ParticleTypes.ELECTRIC_SPARK),
        CRYO(4, "cryo", "§3Cryo", 0xFF00E5FF, ParticleTypes.SNOWFLAKE),
        GEO(5, "geo", "§6Geo", 0xFFFFB300, ParticleTypes.CRIT),
        DENDRO(6, "dendro", "§2Dendro", 0xFF43A047, ParticleTypes.HAPPY_VILLAGER);

        private final int id;
        private final String name;
        private final String displayName;
        private final int color;
        private final ParticleOptions particle;

        Element(int id, String name, String displayName, int color, ParticleOptions particle) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
            this.color = color;
            this.particle = particle;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public int getColor() { return color; }
        public ParticleOptions getParticle() { return particle; }

        public static Element byId(int id) {
            for (Element e : values()) {
                if (e.id == id) return e;
            }
            return HYDRO;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ELEMENT =
            SynchedEntityData.defineId(GenshinSlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_BIG =
            SynchedEntityData.defineId(GenshinSlimeEntity.class, EntityDataSerializers.BOOLEAN);

    public float targetSquish;
    public float squish;
    public float prevSquish;
    private boolean wasOnGround;
    private int airborneTicks = 0;
    private int impactTicks = 0;
    private int attackCooldown = 0;

    public GenshinSlimeEntity(EntityType<? extends GenshinSlimeEntity> type, Level level) {
        super(type, level);
        this.moveControl = new SlimeMoveControl(this);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ELEMENT, Element.HYDRO.getId());
        builder.define(DATA_IS_BIG, false);
    }

    public Element getElement() {
        return Element.byId(this.entityData.get(DATA_ELEMENT));
    }

    public void setElement(Element element) {
        this.entityData.set(DATA_ELEMENT, element.getId());
        updateCustomName();
    }

    public boolean isBig() {
        return this.entityData.get(DATA_IS_BIG);
    }

    public void setBig(boolean big) {
        this.entityData.set(DATA_IS_BIG, big);
        this.refreshDimensions();
        if (big) {
            var hp = this.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) {
                hp.setBaseValue(75.0D);
                this.setHealth(75.0F);
            }
            var dmg = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) dmg.setBaseValue(12.0D);
            var kb = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) kb.setBaseValue(0.6D);
            var step = this.getAttribute(Attributes.STEP_HEIGHT);
            if (step != null) step.setBaseValue(1.25D);
            this.xpReward = 25;
        } else {
            var step = this.getAttribute(Attributes.STEP_HEIGHT);
            if (step != null) step.setBaseValue(1.0D);
        }
        updateCustomName();
    }

    private void updateCustomName() {
        Element el = getElement();
        String sizePrefix = isBig() ? "§lSlime " + el.getDisplayName() + " Gigante" : "Slime " + el.getDisplayName();
        this.setCustomName(Component.literal(sizePrefix));
        this.setCustomNameVisible(false); // No tapar la barra de vida flotante
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return isBig() ? EntityDimensions.scalable(1.65F, 1.45F) : EntityDimensions.scalable(0.85F, 0.75F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new SlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LeyLineMonolithEntity.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.IronGolem.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Slimes elásticos inmunes al daño por caída
        if (fallDistance > 1.5F) {
            this.targetSquish = isBig() ? -1.45F : -1.25F;
            this.playSound(getSquishSound(), isBig() ? 0.9F : 0.65F, 0.85F);
            emitElementParticles(isBig() ? 12 : 6);
        }
        return false;
    }

    @Override
    protected int calculateFallDamage(float distance, float damageMultiplier) {
        return 0;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    /**
     * Salto elástico parabólico (Squash & Stretch Physics de IsekaiSlime)
     */
    public void hop(boolean aggressive) {
        this.targetSquish = 1.0F;
        this.impactTicks = 0;
        float yawRad = this.getYRot() * ((float) Math.PI / 180.0F);
        float forwardSpeed = aggressive ? (isBig() ? 0.44F : 0.38F) : (isBig() ? 0.26F : 0.22F);
        float jumpPower = aggressive ? (isBig() ? 0.48F : 0.44F) : (isBig() ? 0.42F : 0.38F);

        Vec3 delta = this.getDeltaMovement();
        this.setDeltaMovement(
                delta.x - (double) (Mth.sin(yawRad) * forwardSpeed),
                (double) jumpPower,
                delta.z + (double) (Mth.cos(yawRad) * forwardSpeed)
        );
        this.hasImpulse = true;

        this.playSound(getJumpSound(), isBig() ? 0.85F : 0.55F,
                ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * (isBig() ? 0.75F : 0.9F));
    }

    @Override
    public void tick() {
        this.prevSquish = this.squish;

        super.tick();

        boolean onGround = this.onGround();
        // Al impactar contra el suelo tras un salto:
        if (onGround) {
            if (this.airborneTicks >= 3 && this.getDeltaMovement().y <= 0.05D) {
                this.targetSquish = isBig() ? -1.45F : -1.25F;
                this.impactTicks = 14;
                this.playSound(getSquishSound(), isBig() ? 0.9F : 0.65F,
                        ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * (isBig() ? 0.8F : 0.95F));
                emitElementParticles(isBig() ? 14 : 8);
            }
            this.airborneTicks = 0;
        } else {
            this.airborneTicks++;
            if (this.wasOnGround && this.getDeltaMovement().y > 0.1D) {
                this.targetSquish = 1.0F;
                this.impactTicks = 0;
            } else if (this.getDeltaMovement().y > 0.05D) {
                this.targetSquish = 0.85F;
            } else if (this.getDeltaMovement().y < -0.05D) {
                this.targetSquish = 0.1F;
            }
        }

        // Rebotes elásticos fluidos tras el impacto (damping oscilatorio)
        if (this.impactTicks > 0) {
            this.impactTicks--;
            if (this.impactTicks == 10) {
                this.targetSquish = 0.5F;
            } else if (this.impactTicks == 6) {
                this.targetSquish = -0.25F;
            } else if (this.impactTicks == 2) {
                this.targetSquish = 0.05F;
            }
        }

        this.wasOnGround = onGround;
        this.squish += (this.targetSquish - this.squish) * 0.45F;
        this.targetSquish *= 0.8F;

        // Fricción en el suelo para asentarse y lucir la animación squash antes de saltar de nuevo
        if (onGround && !this.isVehicle()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.72D, 1.0D, 0.72D));
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        // Mantener aura elemental innata del Slime en el servidor
        if (!this.level().isClientSide && this.tickCount % 40 == 0) {
            com.soybe.awakeningsystem.element.ElementType elemType = com.soybe.awakeningsystem.element.ElementType.fromString(getElement().getName());
            if (elemType != null) {
                var a = com.soybe.awakeningsystem.element.ElementalReactionManager.getAura(this);
                if (a == null || a.primary == null) {
                    com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(this, elemType, null, 0);
                }
            }
        }

        // Efectos pasivos de partículas según el Elemento de Teyvat
        if (this.level().isClientSide && this.random.nextInt(6) == 0) {
            double spread = isBig() ? 0.8D : 0.4D;
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * spread;
            double py = this.getY() + 0.2D + this.random.nextDouble() * (isBig() ? 0.8D : 0.4D);
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * spread;
            this.level().addParticle(getElement().getParticle(), px, py, pz, 0, 0.01D, 0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Estabilización en agua / lava
        if (this.isInWater() || this.isInLava()) {
            if (this.isUnderWater()) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.08D, this.getDeltaMovement().z);
            } else if (this.getDeltaMovement().y < 0.0D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z);
            }
        }
    }

    public void emitElementParticles(int count) {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(getElement().getParticle(),
                    this.getX(), this.getY() + 0.2D, this.getZ(),
                    count, 0.3D, 0.1D, 0.3D, 0.05D);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            Element el = getElement();
            com.soybe.awakeningsystem.element.ElementType elemType = com.soybe.awakeningsystem.element.ElementType.fromString(el.getName());
            if (elemType != null) {
                com.soybe.awakeningsystem.element.ElementalReactionManager.applyElement(living, elemType, this, isBig() ? 8.0F : 4.0F);
            }
            switch (el) {
                case PYRO -> {
                    living.igniteForSeconds(isBig() ? 6 : 3);
                    this.playSound(SoundEvents.FIRECHARGE_USE, 1.0F, 1.2F);
                }
                case HYDRO -> {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, isBig() ? 2 : 1));
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                    this.playSound(SoundEvents.PLAYER_SPLASH, 1.0F, 1.4F);
                }
                case ANEMO -> {
                    Vec3 dir = target.position().subtract(this.position()).normalize();
                    living.knockback(isBig() ? 1.5D : 0.8D, -dir.x, -dir.z);
                    this.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), 1.0F, 1.2F);
                }
                case ELECTRO -> {
                    living.hurt(this.damageSources().lightningBolt(), isBig() ? 6.0F : 3.0F);
                    this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.4F, 2.0F);
                }
                case CRYO -> {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, isBig() ? 3 : 1));
                    living.setTicksFrozen(isBig() ? 140 : 80);
                    this.playSound(SoundEvents.GLASS_BREAK, 0.8F, 1.6F);
                }
                case GEO -> {
                    living.knockback(isBig() ? 1.2D : 0.6D, Math.sin(Math.toRadians(this.getYRot())), -Math.cos(Math.toRadians(this.getYRot())));
                    this.playSound(SoundEvents.STONE_HIT, 1.0F, 0.8F);
                }
                case DENDRO -> {
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, isBig() ? 1 : 0));
                    this.heal(isBig() ? 8.0F : 4.0F);
                    this.playSound(SoundEvents.GRASS_HIT, 1.0F, 1.2F);
                }
            }
            emitElementParticles(12);
        }
        return hurt;
    }

    public void performSpecialElementalAttack(LivingEntity target) {
        if (this.attackCooldown > 0 || !this.isAlive()) return;
        this.attackCooldown = isBig() ? 60 : 90;

        Element el = getElement();
        switch (el) {
            case PYRO -> {
                double dx = target.getX() - this.getX();
                double dy = target.getY(0.5D) - this.getY(0.5D);
                double dz = target.getZ() - this.getZ();
                SmallFireball fireball = new SmallFireball(this.level(), this, new Vec3(dx, dy, dz));
                fireball.setPos(this.getX(), this.getY() + 0.6D, this.getZ());
                this.level().addFreshEntity(fireball);
                this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.4F);
            }
            case CRYO -> {
                double dx = target.getX() - this.getX();
                double dy = target.getY(0.5D) - this.getY(0.5D);
                double dz = target.getZ() - this.getZ();
                Snowball snowball = new Snowball(this.level(), this);
                snowball.setPos(this.getX(), this.getY() + 0.6D, this.getZ());
                snowball.shoot(dx, dy, dz, 1.2F, 2.0F);
                this.level().addFreshEntity(snowball);
                this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 1.2F);
            }
            case ANEMO -> {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.65D, 0));
                this.hasImpulse = true;
                this.playSound(SoundEvents.BREEZE_IDLE_AIR, 1.0F, 1.4F);
            }
            default -> {}
        }
    }

    @Override
    public SoundEvent getAmbientSound() {
        return ModSounds.GENSHIN_SLIME_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.GENSHIN_SLIME_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GENSHIN_SLIME_DEATH.get();
    }

    protected SoundEvent getSquishSound() {
        return ModSounds.GENSHIN_SLIME_JUMP.get();
    }

    protected SoundEvent getJumpSound() {
        return ModSounds.GENSHIN_SLIME_JUMP.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Element", getElement().getId());
        tag.putBoolean("IsBig", isBig());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Element")) {
            setElement(Element.byId(tag.getInt("Element")));
        }
        if (tag.contains("IsBig")) {
            setBig(tag.getBoolean("IsBig"));
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (this.random.nextFloat() < 0.25F) {
            setBig(true);
        }
        updateCustomName();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    // --- AI MOVE CONTROL (SALTOS RÍTMICOS DE ISEKAI SLIME) ---
    public static class SlimeMoveControl extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final GenshinSlimeEntity slime;
        private boolean isAggressive;

        public SlimeMoveControl(GenshinSlimeEntity slime) {
            super(slime);
            this.slime = slime;
            this.yRot = 180.0F * slime.getYRot() / (float) Math.PI;
            this.jumpDelay = 10 + slime.getRandom().nextInt(15);
        }

        public void setDirection(float yRot, boolean aggressive) {
            this.yRot = yRot;
            this.isAggressive = aggressive;
        }

        public void setWantedMovement(double speed) {
            this.speedModifier = speed;
            this.operation = Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            if (this.slime.isVehicle()) {
                return;
            }

            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();

            if (this.operation != Operation.MOVE_TO) {
                this.mob.setZza(0.0F);
                return;
            }

            if (this.slime.isInWater() || this.slime.isInLava()) {
                double vy = this.slime.isUnderWater() ? 0.08D : 0.0D;
                if (this.operation == Operation.MOVE_TO) {
                    float rad = this.yRot * ((float) Math.PI / 180.0F);
                    double spd = this.isAggressive ? 0.32D : 0.20D;
                    this.slime.setDeltaMovement(-Math.sin(rad) * spd, vy, Math.cos(rad) * spd);
                } else {
                    Vec3 v = this.slime.getDeltaMovement();
                    this.slime.setDeltaMovement(v.x * 0.75D, vy, v.z * 0.75D);
                }
                this.operation = Operation.WAIT;
                return;
            }

            this.operation = Operation.WAIT;
            if (this.mob.onGround()) {
                if (this.jumpDelay-- <= 0) {
                    this.jumpDelay = this.isAggressive ? (5 + this.slime.getRandom().nextInt(6)) : (12 + this.slime.getRandom().nextInt(16));
                    this.slime.hop(this.isAggressive);
                } else {
                    this.slime.setDeltaMovement(this.slime.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                }
            }
        }
    }

    static class SlimeAttackGoal extends Goal {
        private final GenshinSlimeEntity slime;
        private int growTiredTicks;
        private int attackCooldown = 0;

        public SlimeAttackGoal(GenshinSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.slime.isVehicle()) return false;
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive()) return false;
            return !(target instanceof Player player && player.isSpectator());
        }

        @Override
        public void start() {
            this.growTiredTicks = 300;
            this.attackCooldown = 0;
            super.start();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.slime.isVehicle()) return false;
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (target instanceof Player player && (player.isSpectator() || player.isCreative())) return false;
            return --this.growTiredTicks > 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.slime.getTarget();
            if (target == null) return;
            this.slime.lookAt(target, 15.0F, 15.0F);

            if (this.slime.getMoveControl() instanceof SlimeMoveControl moveCtrl) {
                moveCtrl.setDirection(this.slime.getYRot(), true);
                moveCtrl.setWantedMovement(1.25D);
            }

            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }

            double distSq = this.slime.distanceToSqr(target);
            double reach = this.slime.isBig() ? 5.0D : 3.2D;
            if (this.attackCooldown <= 0 && distSq < reach && this.slime.hasLineOfSight(target)) {
                if (this.slime.doHurtTarget(target)) {
                    this.slime.playSound(SoundEvents.SLIME_ATTACK, 1.0F,
                            ((this.slime.getRandom().nextFloat() - this.slime.getRandom().nextFloat()) * 0.2F + 1.0F));
                    this.attackCooldown = 20;
                }
            } else if (distSq < 64.0D && this.slime.random.nextFloat() < 0.08F) {
                this.slime.performSpecialElementalAttack(target);
            }
        }
    }

    static class SlimeRandomDirectionGoal extends Goal {
        private final GenshinSlimeEntity slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public SlimeRandomDirectionGoal(GenshinSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.slime.isVehicle()) return false;
            return this.slime.getTarget() == null && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava());
        }

        @Override
        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = 30 + this.slime.getRandom().nextInt(50);
                this.chosenDegrees = (float) this.slime.getRandom().nextInt(360);
            }
            if (this.slime.getMoveControl() instanceof SlimeMoveControl moveCtrl) {
                moveCtrl.setDirection(this.chosenDegrees, false);
                moveCtrl.setWantedMovement(1.0D);
            }
        }
    }
}
