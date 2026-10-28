package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.item.CompanionContractItem;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class CompanionEntity extends TamableAnimal {
    private static final EntityDataAccessor<String> PROFILE_ID =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_GUARDING =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COMPANION_LEVEL =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COMPANION_CONSTELLATION =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TALENT_NORMAL_ATTACK =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TALENT_ELEMENTAL_SKILL =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TALENT_ELEMENTAL_BURST =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COMPANION_ASCENSION_RANK =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> COMPANION_EXP =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.LONG);

    private int skillCooldownTicks = 0;
    private long lastHurtVoiceTick = 0;
    private long lastInteractVoiceTick = 0;
    private final net.minecraft.core.NonNullList<ItemStack> artifactSlots = net.minecraft.core.NonNullList.withSize(5, ItemStack.EMPTY);

    public net.minecraft.core.NonNullList<ItemStack> getArtifactSlots() {
        return artifactSlots;
    }

    public ItemStack getArtifact(int slot) {
        if (slot >= 0 && slot < 5) return artifactSlots.get(slot);
        return ItemStack.EMPTY;
    }

    public void setArtifact(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 5) {
            artifactSlots.set(slot, stack == null ? ItemStack.EMPTY : stack);
            recalculateCompanionStats();
        }
    }

    public void recalculateCompanionStats() {
        int level = getCompanionLevel();
        int constel = getConstellation();
        int ascRank = getAscensionRank();
        int tAttack = getTalentNormalAttack();

        float bonusHp = 0.0F;
        float bonusAtk = 0.0F;
        ItemStack flower = getArtifact(0);
        if (!flower.isEmpty() && flower.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem art) {
            bonusHp += art.getHpBonus(flower);
        }
        ItemStack plume = getArtifact(1);
        if (!plume.isEmpty() && plume.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem art) {
            bonusAtk += art.getAttackBonus(plume);
        }

        double rankMult = 1.0D + ascRank * 0.15D;
        double constelDmgMult = 1.0D;
        double constelHpBonus = 0.0D;
        if (constel >= 1) constelDmgMult += 0.15D;
        if (constel >= 4) {
            constelHpBonus += 80.0D;
            bonusHp *= 1.25F;
        }
        if (constel > 6) {
            int over = constel - 6;
            constelDmgMult += over * 0.05D;
            constelHpBonus += over * 50.0D;
        }

        double baseHp = (300.0D + (level - 1) * 10.0D + constelHpBonus) * rankMult + bonusHp;
        double baseAtk = (18.0D + (level - 1) * 2.0D + (tAttack - 1) * 3.5D) * rankMult * constelDmgMult + bonusAtk;

        var healthAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(baseHp);
        }
        var attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(baseAtk);
        }
    }

    public CompanionEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    public CompanionEntity(EntityType<? extends TamableAnimal> entityType, Level level, Player owner, CompanionProfile profile) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.tame(owner);
        this.setCompanionProfile(profile);
        this.recalculateCompanionStats();
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData spawnGroupData) {
        var data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        this.recalculateCompanionStats();
        this.setHealth(this.getMaxHealth());
        return data;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.15D, 6.0F, 2.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                if (mob.getLastHurtByMob() instanceof Player p && p.getUUID().equals(((CompanionEntity) mob).getOwnerUUID())) {
                    return false;
                }
                return super.canUse();
            }
        });
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player p && p.getUUID().equals(this.getOwnerUUID())) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Player p && p.getUUID().equals(this.getOwnerUUID())) {
            return false;
        }
        return super.wantsToAttack(target, owner);
    }

    @Override
    public void setTarget(LivingEntity target) {
        if (target instanceof Player p && p.getUUID().equals(this.getOwnerUUID())) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PROFILE_ID, CompanionProfile.NOELLE.id());
        builder.define(IS_GUARDING, false);
        builder.define(COMPANION_LEVEL, 1);
        builder.define(COMPANION_CONSTELLATION, 0);
        builder.define(TALENT_NORMAL_ATTACK, 1);
        builder.define(TALENT_ELEMENTAL_SKILL, 1);
        builder.define(TALENT_ELEMENTAL_BURST, 1);
        builder.define(COMPANION_ASCENSION_RANK, 0);
        builder.define(COMPANION_EXP, 0L);
    }

    public int getCompanionLevel() {
        return this.entityData.get(COMPANION_LEVEL);
    }

    public void setCompanionLevel(int level) {
        int clamped = Math.max(1, Math.min(1000, level));
        this.entityData.set(COMPANION_LEVEL, clamped);
        recalculateCompanionStats();
        updateCustomName();
    }

    public int getConstellation() {
        return this.entityData.get(COMPANION_CONSTELLATION);
    }

    public void setConstellation(int c) {
        this.entityData.set(COMPANION_CONSTELLATION, Math.max(0, c));
        recalculateCompanionStats();
        updateCustomName();
    }

    public int getTalentNormalAttack() {
        return this.entityData.get(TALENT_NORMAL_ATTACK);
    }

    public void setTalentNormalAttack(int lvl) {
        this.entityData.set(TALENT_NORMAL_ATTACK, Math.max(1, lvl));
        recalculateCompanionStats();
    }

    public int getTalentElementalSkill() {
        return this.entityData.get(TALENT_ELEMENTAL_SKILL);
    }

    public void setTalentElementalSkill(int lvl) {
        this.entityData.set(TALENT_ELEMENTAL_SKILL, Math.max(1, lvl));
        recalculateCompanionStats();
    }

    public int getTalentElementalBurst() {
        return this.entityData.get(TALENT_ELEMENTAL_BURST);
    }

    public void setTalentElementalBurst(int lvl) {
        this.entityData.set(TALENT_ELEMENTAL_BURST, Math.max(1, lvl));
        recalculateCompanionStats();
    }

    public int getAscensionRank() {
        return this.entityData.get(COMPANION_ASCENSION_RANK);
    }

    public void setAscensionRank(int rank) {
        this.entityData.set(COMPANION_ASCENSION_RANK, Math.max(0, rank));
        recalculateCompanionStats();
    }

    public long getCompanionExp() {
        return this.entityData.get(COMPANION_EXP);
    }

    public void setCompanionExp(long exp) {
        this.entityData.set(COMPANION_EXP, Math.max(0L, exp));
    }

    public CompanionProfile getCompanionProfile() {
        return CompanionProfile.getById(this.entityData.get(PROFILE_ID));
    }

    public void setCompanionProfile(CompanionProfile profile) {
        if (profile == null) profile = CompanionProfile.NOELLE;
        this.entityData.set(PROFILE_ID, profile.id());
        this.updateCustomName();
        this.updateEquipmentForProfile();
        this.refreshDimensions();
    }

    @Override
    public float getScale() {
        return this.getCompanionProfile() != null ? this.getCompanionProfile().getModelScale() : 1.0F;
    }

    public void updateEquipmentForProfile() {
        CompanionProfile p = getCompanionProfile();
        ItemStack weapon = p.getSignatureWeapon();
        this.setItemSlot(EquipmentSlot.MAINHAND, weapon.copy());
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public void updateCustomName() {
        CompanionProfile p = getCompanionProfile();
        String starsStr = "★".repeat(p.stars());
        int c = getConstellation();
        String cStr = c > 0 ? " §d[C" + c + "]" : "";
        this.setCustomName(Component.literal(p.colorCode() + p.displayName() + " §e[" + starsStr + "]" + cStr + " §fNv." + getCompanionLevel()));
        this.setCustomNameVisible(true);
    }

    public boolean isGuarding() {
        return this.entityData.get(IS_GUARDING);
    }

    public void setGuarding(boolean guarding) {
        this.entityData.set(IS_GUARDING, guarding);
        this.setOrderedToSit(guarding);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.getMainHandItem().isEmpty()) {
            this.updateEquipmentForProfile();
        }
        if (this.skillCooldownTicks > 0) {
            this.skillCooldownTicks--;
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.level().isClientSide() && this.skillCooldownTicks <= 0) {
            triggerElementalSkill(target);
            this.skillCooldownTicks = 160;
        }
    }

    private void triggerElementalSkill(LivingEntity target) {
        CompanionProfile profile = getCompanionProfile();
        LivingEntity owner = this.getOwner();
        ServerLevel sl = (ServerLevel) this.level();

        if (owner instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
        }
        this.playSound(profile.getSkillSound(), 1.2F, 1.0F);

        switch (profile) {
            case COLUMBINA -> {
                AABB box = target.getBoundingBox().inflate(6.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().magic(), 32.0F);
                    nearby.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, false));
                }
                sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.2D, target.getZ(), 45, 1.2D, 1.5D, 1.2D, 0.1D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 0.8D, target.getZ(), 30, 0.8D, 1.0D, 0.8D, 0.05D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.4F);
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, false, true));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 0, false, false, true));
                    sp.displayClientMessage(Component.translatable("companion.awakening_system.columbina_protect"), true);
                }
            }
            case RAIDEN_SHOGUN -> {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                if (bolt != null) {
                    bolt.moveTo(target.position());
                    bolt.setVisualOnly(true);
                    sl.addFreshEntity(bolt);
                }
                target.hurt(this.damageSources().mobAttack(this), 28.0F);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.8D, 1.0D, 0.8D, 0.2D);
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1, false, false));
                }
                this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.2F, 1.2F);
            }
            case HU_TAO -> {
                target.setRemainingFireTicks(160);
                target.hurt(this.damageSources().onFire(), 24.0F);
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 50, 0.6D, 0.8D, 0.6D, 0.15D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.2D, target.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);
                this.playSound(SoundEvents.FIRECHARGE_USE, 1.0F, 1.0F);
            }
            case ZHONGLI -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 3, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false));
                }
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, false));
                target.hurt(this.damageSources().mobAttack(this), 20.0F);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.6D, 0.8D, 0.6D, 0.2D);
                this.playSound(SoundEvents.ANVIL_LAND, 1.0F, 0.8F);
            }
            case FURINA -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, false));
                }
                target.hurt(this.damageSources().mobAttack(this), 18.0F);
                target.knockback(1.5D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.0D, 1.0D, 1.0D, 0.2D);
                this.playSound(SoundEvents.PLAYER_SPLASH, 1.2F, 1.2F);
            }
            case NAHIDA -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 4, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1, false, false));
                target.hurt(this.damageSources().magic(), 18.0F);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.8D, 1.0D, 0.8D, 0.1D);
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2F, 1.5F);
            }
            case KAZUHA -> {
                AABB pullBox = target.getBoundingBox().inflate(6.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, pullBox)) {
                    if (nearby != this && nearby != owner && nearby.isAlive()) {
                        nearby.setDeltaMovement(nearby.getDeltaMovement().add(0, 0.65D, 0));
                        nearby.hurt(this.damageSources().mobAttack(this), 16.0F);
                    }
                }
                sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.5D, this.getZ(), 50, 1.2D, 0.5D, 1.2D, 0.1D);
                this.playSound(SoundEvents.ELYTRA_FLYING, 1.0F, 1.2F);
            }
            case SUNG_JINWOO -> {
                target.hurt(this.damageSources().mobAttack(this), 26.0F);
                sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);
                this.playSound(SoundEvents.WITHER_SHOOT, 0.9F, 1.4F);
            }
            case GOJO_SATORU -> {
                target.knockback(2.8D, this.getX() - target.getX(), this.getZ() - target.getZ());
                target.hurt(this.damageSources().magic(), 32.0F);
                sl.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
                this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 0.8F, 1.2F);
            }
            case NOELLE -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, false));
                    sp.heal(4.0F);
                }
                target.hurt(this.damageSources().mobAttack(this), 14.0F);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.5D, 0.7D, 0.5D, 0.1D);
                this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.0F);
            }
            case CHA_HAEIN -> {
                target.hurt(this.damageSources().mobAttack(this), 22.0F);
                sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.15D);
                this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 1.3F);
            }
            case DILUC -> {
                target.setRemainingFireTicks(200);
                target.hurt(this.damageSources().onFire(), 30.0F);
                target.knockback(2.4D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.0D, 1.0D, 1.0D, 0.2D);
                sl.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 1.2D, target.getZ(), 20, 0.8D, 0.8D, 0.8D, 0.1D);
                this.playSound(SoundEvents.BLAZE_SHOOT, 1.2F, 0.8F);
            }
            case JEAN -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.heal(16.0F);
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, false));
                }
                AABB pushBox = this.getBoundingBox().inflate(6.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, pushBox)) {
                    if (nearby != this && nearby != owner && nearby.isAlive()) {
                        nearby.knockback(2.0D, this.getX() - nearby.getX(), this.getZ() - nearby.getZ());
                        nearby.hurt(this.damageSources().mobAttack(this), 16.0F);
                    }
                }
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0D, this.getZ(), 50, 2.0D, 1.0D, 2.0D, 0.1D);
                sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.5D, this.getZ(), 60, 2.5D, 0.5D, 2.5D, 0.15D);
                this.playSound(SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 1.2F, 1.2F);
            }
            case BENNETT -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 1, false, false));
                }
                target.setRemainingFireTicks(120);
                target.hurt(this.damageSources().mobAttack(this), 18.0F);
                sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.2D, this.getZ(), 40, 2.0D, 0.2D, 2.0D, 0.05D);
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.5D, this.getZ(), 10, 0.8D, 0.8D, 0.8D, 0.1D);
                this.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.0F, 1.1F);
            }
            case ACHERON -> {
                target.hurt(this.damageSources().magic(), 34.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, false));
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 50, 1.0D, 1.5D, 1.0D, 0.25D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.8D, 1.0D, 0.8D, 0.1D);
                this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.2F, 1.3F);
            }
            case KAFKA -> {
                target.hurt(this.damageSources().magic(), 26.0F);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 140, 0, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, false));
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 0.8D, 0.8D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.1D);
                this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 0.8F, 1.5F);
            }
            case ELLEN_JOE -> {
                target.hurt(this.damageSources().mobAttack(this), 24.0F);
                target.setTicksFrozen(140);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 3, false, false));
                sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 0.8D, 0.8D, 0.8D, 0.15D);
                sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.6D, 0.6D, 0.6D, 0.2D);
                this.playSound(SoundEvents.GLASS_BREAK, 1.2F, 1.6F);
            }
            case IGRIS -> {
                this.teleportTo(target.getX() - 1.2D, target.getY(), target.getZ() - 1.2D);
                target.hurt(this.damageSources().mobAttack(this), 30.0F);
                target.knockback(1.8D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.7D, 1.0D, 0.7D, 0.1D);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.6D, 0.8D, 0.6D, 0.2D);
                this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.3F, 0.8F);
            }
            case BERU -> {
                target.hurt(this.damageSources().mobAttack(this), 28.0F);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 2, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, false));
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.8D, 0.8D, 0.8D, 0.15D);
                sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.0D, this.getZ(), 1, 0, 0, 0, 0);
                this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.6F);
            }
            case SUKUNA -> {
                target.hurt(this.damageSources().mobAttack(this), 36.0F);
                target.setRemainingFireTicks(180);
                target.knockback(2.2D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.1D);
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 50, 1.0D, 1.2D, 1.0D, 0.2D);
                sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0D, target.getZ(), 2, 0.4D, 0.4D, 0.4D, 0.05D);
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.2F, 1.1F);
            }
            case YUJI_ITADORI -> {
                target.hurt(this.damageSources().mobAttack(this), 22.0F);
                target.knockback(3.0D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.6D, 0.6D, 0.6D, 0.2D);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.8D, 0.8D, 0.8D, 0.3D);
                this.playSound(SoundEvents.ANVIL_LAND, 1.0F, 1.6F);
            }
            case ARTORIA_PENDRAGON -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, false));
                }
                target.hurt(this.damageSources().magic(), 35.0F);
                target.knockback(2.0D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0D, this.getZ(), 50, 1.5D, 1.0D, 1.5D, 0.15D);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.8D, 1.2D, 0.8D, 0.2D);
                this.playSound(SoundEvents.BELL_RESONATE, 1.3F, 1.2F);
            }
            case GILGAMESH -> {
                target.hurt(this.damageSources().mobAttack(this), 32.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, false));
                sl.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY() + 1.2D, target.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.2D);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.6D, 0.6D, 0.6D, 0.2D);
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F, 1.5F);
            }
            case RENGOKU -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1, false, false));
                }
                target.setRemainingFireTicks(200);
                target.hurt(this.damageSources().onFire(), 32.0F);
                target.knockback(2.5D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.2D, 1.2D, 1.2D, 0.25D);
                sl.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.8D, 0.8D, 0.8D, 0.1D);
                this.playSound(SoundEvents.FIRECHARGE_USE, 1.3F, 0.8F);
            }
            case TANJIRO -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 180, 1, false, false));
                }
                target.hurt(this.damageSources().mobAttack(this), 20.0F);
                target.setRemainingFireTicks(80);
                sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.8D, 0.8D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.6D, 0.6D, 0.6D, 0.15D);
                this.playSound(SoundEvents.PLAYER_SPLASH, 1.1F, 1.3F);
            }
            case NEZUKO -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.heal(8.0F);
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, false));
                }
                target.hurt(this.damageSources().magic(), 18.0F);
                target.setRemainingFireTicks(100);
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.2D, this.getZ(), 10, 0.6D, 0.6D, 0.6D, 0.1D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.8D, 0.8D, 0.8D, 0.2D);
                this.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.1F, 1.4F);
            }
            case XIAO -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.JUMP, 160, 2, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, false));
                }
                target.hurt(this.damageSources().mobAttack(this), 34.0F);
                target.knockback(2.6D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.5D, target.getZ(), 50, 1.2D, 1.2D, 1.2D, 0.2D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 6, 0.6D, 0.6D, 0.6D, 0.1D);
                this.playSound(SoundEvents.ELYTRA_FLYING, 1.3F, 1.4F);
            }
            case ARLECCHINO -> {
                target.hurt(this.damageSources().onFire(), 36.0F);
                target.setRemainingFireTicks(180);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, false));
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 50, 0.9D, 1.2D, 0.9D, 0.2D);
                sl.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 1.2D, target.getZ(), 25, 0.6D, 0.6D, 0.6D, 0.1D);
                this.playSound(SoundEvents.BLAZE_SHOOT, 1.2F, 0.7F);
            }
            case FIREFLY -> {
                target.hurt(this.damageSources().mobAttack(this), 35.0F);
                target.setRemainingFireTicks(200);
                target.knockback(2.8D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.2D, 1.2D, 1.2D, 0.25D);
                sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0D, target.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.1D);
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.1F, 1.3F);
            }
            case MEGUMI -> {
                target.hurt(this.damageSources().magic(), 22.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
                sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 0.6D, 0.8D, 0.6D, 0.15D);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.5D, 0.8D, 0.5D, 0.15D);
                this.playSound(SoundEvents.LIGHTNING_BOLT_IMPACT, 1.0F, 1.5F);
            }
            case ARCHER -> {
                target.hurt(this.damageSources().mobAttack(this), 24.0F);
                target.knockback(1.8D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 0.8D, 1.0D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.1D);
                this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.2F, 1.1F);
            }
            case HOSHIMI_MIYABI -> {
                target.hurt(this.damageSources().mobAttack(this), 34.0F);
                target.setTicksFrozen(120);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, false));
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 0.8D, 0.8D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 6, 0.6D, 0.6D, 0.6D, 0.1D);
                this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.4F, 1.6F);
            }
            case JANE_DOE -> {
                target.hurt(this.damageSources().mobAttack(this), 28.0F);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1, false, false));
                target.knockback(1.5D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.6D, 0.8D, 0.6D, 0.25D);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.2D);
                this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.3F, 1.5F);
            }
            case CAESAR_KING -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 2, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1, false, false));
                }
                AABB box = this.getBoundingBox().inflate(5.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().mobAttack(this), 26.0F);
                    nearby.knockback(2.2D, this.getX() - nearby.getX(), this.getZ() - nearby.getZ());
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false));
                }
                sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(), 3, 0.6D, 0.6D, 0.6D, 0.1D);
                sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0D, this.getZ(), 50, 1.2D, 0.8D, 1.2D, 0.3D);
                this.playSound(SoundEvents.SHIELD_BLOCK, 1.5F, 0.9F);
            }
            case BURNICE_WHITE -> {
                AABB box = target.getBoundingBox().inflate(3.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().onFire(), 30.0F);
                    nearby.setRemainingFireTicks(160);
                }
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.0D, 1.2D, 1.0D, 0.2D);
                sl.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);
                this.playSound(SoundEvents.BLAZE_SHOOT, 1.3F, 1.2F);
            }
            case NICOLE_DEMARA -> {
                AABB box = target.getBoundingBox().inflate(6.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().magic(), 22.0F);
                    nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, false));
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
                    double dx = target.getX() - nearby.getX();
                    double dz = target.getZ() - nearby.getZ();
                    nearby.push(dx * 0.25D, 0.1D, dz * 0.25D);
                }
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 70, 1.2D, 1.2D, 1.2D, 0.3D);
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.8D, 0.8D, 0.8D, 0.2D);
                this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.2F, 1.3F);
            }
            case ZHU_YUAN -> {
                target.hurt(this.damageSources().mobAttack(this), 32.0F);
                target.knockback(2.0D, this.getX() - target.getX(), this.getZ() - target.getZ());
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, false));
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 0.6D, 0.6D, 0.6D, 0.2D);
                sl.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.8F);
            }
            case JIYAN -> {
                AABB box = target.getBoundingBox().inflate(4.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().mobAttack(this), 36.0F);
                    nearby.knockback(2.5D, this.getX() - nearby.getX(), this.getZ() - nearby.getZ());
                    nearby.push(0.0D, 0.5D, 0.0D);
                }
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.2D, target.getZ(), 50, 1.2D, 1.2D, 1.2D, 0.2D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.8D, 0.8D, 0.8D, 0.1D);
                sl.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.5D, target.getZ(), 40, 1.5D, 0.5D, 1.5D, 0.2D);
                this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.3F, 1.1F);
                this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.4F, 0.8F);
            }
            case CHANGLI -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 1, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 0, false, false));
                }
                AABB box = target.getBoundingBox().inflate(3.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().onFire(), 35.0F);
                    nearby.setRemainingFireTicks(160);
                }
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.2D, 1.2D, 1.2D, 0.2D);
                sl.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 1.2D, target.getZ(), 20, 0.6D, 0.6D, 0.6D, 0.1D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.8D, 0.8D, 0.8D, 0.15D);
                this.playSound(SoundEvents.BLAZE_SHOOT, 1.2F, 0.8F);
                this.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.2F, 1.2F);
            }
            case JINSHI -> {
                target.hurt(this.damageSources().magic(), 38.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, false));
                target.knockback(2.2D, this.getX() - target.getX(), this.getZ() - target.getZ());
                sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.5D, target.getZ(), 50, 0.8D, 1.5D, 0.8D, 0.25D);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.6D, 0.6D, 0.6D, 0.2D);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.15D);
                this.playSound(SoundEvents.BEACON_ACTIVATE, 1.2F, 1.6F);
                this.playSound(SoundEvents.LIGHTNING_BOLT_IMPACT, 1.0F, 1.8F);
            }
            case YINLIN -> {
                AABB box = target.getBoundingBox().inflate(5.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().magic(), 28.0F);
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 3, false, false));
                }
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.5D, 1.0D, 1.5D, 0.3D);
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.8D, 0.8D, 0.8D, 0.2D);
                this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.1F, 1.5F);
            }
            case SHOREKEEPER -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 2, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1, false, false));
                }
                AABB box = this.getBoundingBox().inflate(6.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().magic(), 26.0F);
                    nearby.push(0.0D, 0.35D, 0.0D);
                }
                sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.2D, this.getZ(), 50, 1.5D, 1.0D, 1.5D, 0.2D);
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.0D, this.getZ(), 45, 1.2D, 1.2D, 1.2D, 0.2D);
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.5D, this.getZ(), 15, 0.8D, 0.8D, 0.8D, 0.1D);
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F, 1.3F);
                this.playSound(SoundEvents.BEACON_POWER_SELECT, 1.2F, 1.4F);
            }
            case CAMELLYA -> {
                AABB box = target.getBoundingBox().inflate(4.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().magic(), 34.0F);
                    nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, false));
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3, false, false));
                }
                this.heal(12.0F);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.0D, target.getZ(), 50, 1.0D, 1.2D, 1.0D, 0.2D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 0.8D, 0.8D, 0.8D, 0.15D);
                sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.2D, target.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.1D);
                this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.3F, 0.8F);
            }
            case ROVER -> {
                AABB box = target.getBoundingBox().inflate(5.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().mobAttack(this), 33.0F);
                    nearby.knockback(2.6D, target.getX() - nearby.getX(), target.getZ() - nearby.getZ());
                    nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, false));
                }
                sl.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 50, 1.2D, 1.2D, 1.2D, 0.25D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 6, 0.8D, 0.8D, 0.8D, 0.1D);
                this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.1F, 1.6F);
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.4F);
            }
            case CLORINDE -> {
                target.hurt(this.damageSources().lightningBolt(), 36.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 2, false, false));
                target.knockback(1.8D, this.getX() - target.getX(), this.getZ() - target.getZ());
                AABB box = target.getBoundingBox().inflate(3.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().magic(), 24.0F);
                }
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 0.8D, 0.8D, 0.8D, 0.3D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.6D, 0.6D, 0.6D, 0.1D);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.2D, target.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.2D);
                this.playSound(SoundEvents.LIGHTNING_BOLT_IMPACT, 1.1F, 1.7F);
                this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.2F, 1.3F);
            }
            case SKIRK -> {
                AABB box = target.getBoundingBox().inflate(6.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().magic(), 38.0F);
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, false));
                    nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, false));
                    double dx = target.getX() - nearby.getX();
                    double dz = target.getZ() - nearby.getZ();
                    nearby.push(dx * 0.3D, 0.1D, dz * 0.3D);
                }
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 80, 1.5D, 1.5D, 1.5D, 0.4D);
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.25D);
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 1.2D, 0.8D, 1.2D, 0.1D);
                this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.3F, 0.6F);
                this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.2F, 0.9F);
            }
            case XILONEN -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 1, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 140, 0, false, false));
                }
                AABB box = target.getBoundingBox().inflate(4.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().mobAttack(this), 30.0F);
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, false));
                    nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, false));
                }
                sl.sendParticles(ParticleTypes.NOTE, target.getX(), target.getY() + 1.5D, target.getZ(), 25, 1.0D, 0.8D, 1.0D, 0.2D);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 1.0D, 1.0D, 1.0D, 0.2D);
                this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.4F, 1.2F);
                this.playSound(SoundEvents.ANVIL_LAND, 0.9F, 1.6F);
            }
            case MAVUIKA -> {
                AABB box = target.getBoundingBox().inflate(5.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().onFire(), 38.0F);
                    nearby.setRemainingFireTicks(180);
                    nearby.knockback(2.2D, this.getX() - nearby.getX(), this.getZ() - nearby.getZ());
                }
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 80, 1.5D, 1.2D, 1.5D, 0.25D);
                sl.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 1.2D, target.getZ(), 30, 0.8D, 0.8D, 0.8D, 0.15D);
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.2F, 1.1F);
                this.playSound(SoundEvents.BLAZE_SHOOT, 1.4F, 0.8F);
            }
            case KINICH -> {
                target.hurt(this.damageSources().mobAttack(this), 37.0F);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, false));
                target.knockback(2.4D, this.getX() - target.getX(), this.getZ() - target.getZ());
                AABB box = target.getBoundingBox().inflate(3.5D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().magic(), 26.0F);
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
                }
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.0D, 1.0D, 1.0D, 0.2D);
                sl.sendParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY() + 1.2D, target.getZ(), 40, 0.8D, 0.8D, 0.8D, 0.15D);
                sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0D, target.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.1D);
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.7F);
                this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.3F, 1.2F);
            }
            case NANALLY -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1, false, false));
                    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0, false, false));
                }
                AABB box = target.getBoundingBox().inflate(6.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().magic(), 35.0F);
                    double dx = target.getX() - nearby.getX();
                    double dz = target.getZ() - nearby.getZ();
                    nearby.push(dx * 0.25D, 0.4D, dz * 0.25D);
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
                }
                sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.5D, 1.2D, 1.5D, 0.3D);
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 1.2D, 1.0D, 1.2D, 0.2D);
                sl.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 0.8D, target.getZ(), 30, 1.0D, 0.8D, 1.0D, 0.1D);
                this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.1F, 1.5F);
                this.playSound(SoundEvents.PORTAL_TRAVEL, 1.3F, 1.7F);
            }
            case MINT -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, false));
                }
                AABB box = target.getBoundingBox().inflate(5.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().lightningBolt(), 24.0F);
                    nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, false));
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1, false, false));
                    nearby.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
                }
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.2D, 1.0D, 1.2D, 0.3D);
                sl.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.5D, target.getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 0.8D, 0.8D, 0.8D, 0.2D);
                this.playSound(SoundEvents.LIGHTNING_BOLT_IMPACT, 1.1F, 1.6F);
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F, 1.8F);
            }
            case LACRIMOSA -> {
                AABB box = target.getBoundingBox().inflate(6.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != owner && !e.isAlliedTo(this))) {
                    nearby.hurt(this.damageSources().wither(), 38.0F);
                    nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1, false, false));
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2, false, false));
                }
                this.heal(14.0F);
                if (owner instanceof ServerPlayer sp) {
                    sp.heal(6.0F);
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false));
                }
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.4D, 1.2D, 1.4D, 0.25D);
                sl.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 1.0D, 1.2D, 1.0D, 0.15D);
                sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 0.8D, target.getZ(), 35, 0.8D, 0.8D, 0.8D, 0.1D);
                sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.2D, target.getZ(), 15, 0.6D, 0.6D, 0.6D, 0.1D);
                this.playSound(SoundEvents.WITHER_SHOOT, 1.2F, 0.7F);
                this.playSound(SoundEvents.SCULK_CATALYST_BLOOM, 1.4F, 1.1F);
            }
        }
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.VOICE;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && this.isOwnedBy(player)) {
            ItemStack held = player.getItemInHand(hand);

            if (held.is(com.soybe.awakeningsystem.SystemItems.HEROS_WIT.get()) || held.is(com.soybe.awakeningsystem.SystemItems.ADVENTURERS_EXPERIENCE.get())) {
                int rankCap = com.soybe.awakeningsystem.entity.CompanionAscensionManager.getRankMaxLevel(getAscensionRank());
                if (getCompanionLevel() >= rankCap) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.phase_cap_reached", rankCap, getCompanionProfile().displayName()), true);
                    return InteractionResult.SUCCESS;
                }
                if (getCompanionLevel() >= 90) {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.max_level_reached", getCompanionProfile().displayName()), true);
                    return InteractionResult.SUCCESS;
                }
                int costMora = getCompanionLevel() * 2500;
                com.soybe.awakeningsystem.SystemData data = com.soybe.awakeningsystem.SystemEvents.data(player);
                if (data.mora() >= costMora) {
                    data.spendMora(costMora);
                    held.shrink(1);
                    setCompanionLevel(getCompanionLevel() + 1);
                    this.heal(25.0F);

                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.0D, this.getZ(), 35, 0.4D, 0.6D, 0.4D, 0.1D);
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 15, 0.5D, 0.5D, 0.5D, 0.05D);
                    }
                    this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.1F);
                    if (player instanceof ServerPlayer sp) {
                        com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    }
                    player.displayClientMessage(Component.translatable("companion.awakening_system.ascended_success", getCompanionProfile().displayName(), getCompanionLevel(), String.format(java.util.Locale.US, "%,d", costMora)), true);
                    return InteractionResult.SUCCESS;
                } else {
                    player.displayClientMessage(Component.translatable("companion.awakening_system.insufficient_mora", String.format(java.util.Locale.US, "%,d", costMora), (getCompanionLevel() + 1)), true);
                    return InteractionResult.CONSUME;
                }
            }

            if (player.isShiftKeyDown()) {
                ItemStack contract = CompanionContractItem.createContract(getCompanionProfile(), getCompanionLevel());
                CompoundTag contractTag = contract.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                net.minecraft.nbt.ListTag artList = new net.minecraft.nbt.ListTag();
                for (int i = 0; i < 5; i++) {
                    ItemStack art = getArtifact(i);
                    if (!art.isEmpty()) {
                        CompoundTag slotTag = new CompoundTag();
                        slotTag.putByte("Slot", (byte) i);
                        artList.add(art.save(player.registryAccess(), slotTag));
                    }
                }
                contractTag.put("CompanionArtifacts", artList);
                contract.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(contractTag));

                if (!player.getInventory().add(contract)) {
                    player.drop(contract, false);
                }
                this.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.2F);
                player.displayClientMessage(Component.translatable("companion.awakening_system.returned_to_contract", getCompanionProfile().displayName()), true);
                this.discard();
                return InteractionResult.SUCCESS;
            } else {
                if (!this.level().isClientSide() && player instanceof ServerPlayer sp) {
                    sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
                    this.playSound(getCompanionProfile().getAmbientSound(), 1.2F, 1.0F);
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("EntityId", this.getId());
                    tag.putString("ProfileId", getCompanionProfile().id());
                    tag.putString("Name", getCompanionProfile().displayName());
                    tag.putInt("Level", getCompanionLevel());
                    tag.putInt("Stars", getCompanionProfile().stars());
                    tag.putString("Element", getCompanionProfile().element());
                    tag.putInt("AscensionRank", getAscensionRank());
                    tag.putFloat("Health", this.getHealth());
                    tag.putFloat("MaxHealth", this.getMaxHealth());
                    tag.putBoolean("Guarding", isGuarding());
                    net.minecraft.nbt.ListTag artList = new net.minecraft.nbt.ListTag();
                    for (int i = 0; i < 5; i++) {
                        ItemStack s = getArtifact(i);
                        CompoundTag slotTag = new CompoundTag();
                        slotTag.putByte("Slot", (byte) i);
                        if (!s.isEmpty()) {
                            artList.add(s.save(sp.registryAccess(), slotTag));
                        }
                    }
                    tag.put("Artifacts", artList);
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new com.soybe.awakeningsystem.network.OpenCompanionScreenPayload(this.getId(), tag));
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getHealth() - amount <= 0.0F) {
            LivingEntity owner = this.getOwner();
            if (owner instanceof Player p && !this.level().isClientSide()) {
                ItemStack contract = CompanionContractItem.createContract(getCompanionProfile(), getCompanionLevel());
                CompoundTag contractTag = contract.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                net.minecraft.nbt.ListTag artList = new net.minecraft.nbt.ListTag();
                for (int i = 0; i < 5; i++) {
                    ItemStack art = getArtifact(i);
                    if (!art.isEmpty()) {
                        CompoundTag slotTag = new CompoundTag();
                        slotTag.putByte("Slot", (byte) i);
                        artList.add(art.save(p.registryAccess(), slotTag));
                    }
                }
                contractTag.put("CompanionArtifacts", artList);
                contract.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(contractTag));

                if (!p.getInventory().add(contract)) {
                    p.drop(contract, false);
                }
                p.displayClientMessage(Component.translatable("companion.awakening_system.exhausted_returned", getCompanionProfile().displayName()), true);
                this.playSound(SoundEvents.TOTEM_USE, 0.8F, 1.5F);
            }
            this.discard();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("ProfileId", getCompanionProfile().id());
        compound.putBoolean("Guarding", isGuarding());
        compound.putInt("CompanionLevel", getCompanionLevel());
        compound.putInt("CompanionConstellation", getConstellation());
        compound.putInt("TalentNormalAttack", getTalentNormalAttack());
        compound.putInt("TalentElementalSkill", getTalentElementalSkill());
        compound.putInt("TalentElementalBurst", getTalentElementalBurst());
        compound.putInt("CompanionAscensionRank", getAscensionRank());
        compound.putLong("CompanionExp", getCompanionExp());
        net.minecraft.nbt.ListTag artList = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < 5; i++) {
            ItemStack s = artifactSlots.get(i);
            if (!s.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                artList.add(s.save(this.registryAccess(), slotTag));
            }
        }
        compound.put("CompanionArtifacts", artList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ProfileId")) {
            setCompanionProfile(CompanionProfile.getById(compound.getString("ProfileId")));
        }
        if (compound.contains("Guarding")) {
            setGuarding(compound.getBoolean("Guarding"));
        }
        if (compound.contains("CompanionLevel")) {
            setCompanionLevel(compound.getInt("CompanionLevel"));
        }
        if (compound.contains("CompanionConstellation")) {
            setConstellation(compound.getInt("CompanionConstellation"));
        }
        if (compound.contains("TalentNormalAttack")) {
            setTalentNormalAttack(compound.getInt("TalentNormalAttack"));
        }
        if (compound.contains("TalentElementalSkill")) {
            setTalentElementalSkill(compound.getInt("TalentElementalSkill"));
        }
        if (compound.contains("TalentElementalBurst")) {
            setTalentElementalBurst(compound.getInt("TalentElementalBurst"));
        }
        if (compound.contains("CompanionAscensionRank")) {
            setAscensionRank(compound.getInt("CompanionAscensionRank"));
        }
        if (compound.contains("CompanionExp")) {
            setCompanionExp(compound.getLong("CompanionExp"));
        }
        artifactSlots.clear();
        for (int i = 0; i < 5; i++) artifactSlots.add(ItemStack.EMPTY);
        if (compound.contains("CompanionArtifacts", net.minecraft.nbt.Tag.TAG_LIST)) {
            net.minecraft.nbt.ListTag artList = compound.getList("CompanionArtifacts", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < artList.size(); i++) {
                CompoundTag slotTag = artList.getCompound(i);
                int s = slotTag.getByte("Slot") & 255;
                if (s < 5) {
                    ItemStack.parse(this.registryAccess(), slotTag).ifPresent(stack -> artifactSlots.set(s, stack));
                }
            }
        }
        recalculateCompanionStats();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        long now = this.level().getGameTime();
        if (now - lastHurtVoiceTick > 60) {
            lastHurtVoiceTick = now;
            return getCompanionProfile().getHurtSound();
        }
        return null;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }
}
