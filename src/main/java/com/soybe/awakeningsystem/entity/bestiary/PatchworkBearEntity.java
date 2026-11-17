package com.soybe.awakeningsystem.entity.bestiary;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The Patchwork Bear (roadmap 14.10): from afar an old patched teddy bear with a button sewn on for an eye,
 * tottering about. When someone comes close (or hurts it) the seams of its belly burst: the chest opens into a
 * maw of needle teeth and cotton stuffing, black claws come out of its paws and its one real eye glows red.
 * Its bite tears armour and leaves the victim weak. It calms down again once it loses its prey.
 */
public class PatchworkBearEntity extends Monster {
    private static final EntityDataAccessor<Boolean> HOSTILE = SynchedEntityData.defineId(PatchworkBearEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double WAKE_RANGE = 8.0;
    private int calmTimer;
    /** How far the maw is open (client side, eased toward the state): 0 plush, 1 open. */
    private float open, openO;

    public PatchworkBearEntity(EntityType<? extends PatchworkBearEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 70.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(HOSTILE, false);
    }

    /** A slower swing, so the lunge and bite can be seen. */
    @Override
    public int getCurrentSwingDuration() {
        return 12;
    }

    public boolean isHostile() {
        return this.entityData.get(HOSTILE);
    }

    public float open(float partialTick) {
        return openO + (open - openO) * partialTick;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false) {
            @Override
            public boolean canUse() { return isHostile() && super.canUse(); }
            @Override
            public boolean canContinueToUse() { return isHostile() && super.canContinueToUse(); }
        });
        // as a toy it only totters about, slowly
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity t = this.getTarget();
        boolean prey = t != null && t.isAlive();
        if (!isHostile()) {
            if (prey && this.distanceToSqr(t) < WAKE_RANGE * WAKE_RANGE) burst();
            return;
        }
        if (prey && this.distanceToSqr(t) < 24 * 24) calmTimer = 0;
        else if (++calmTimer > 400) {   // twenty seconds without prey, not six: no flicking back and forth
            // lost its prey: the seams close again
            this.entityData.set(HOSTILE, false);
            calmTimer = 0;
        }
    }

    /** The seams burst: it turns. */
    public void burst() {
        this.entityData.set(HOSTILE, true);
        calmTimer = 0;
        this.level().playSound(null, this, SoundEvents.WOOL_BREAK, SoundSource.HOSTILE, 1.4F, 0.6F);
        this.level().playSound(null, this, SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 0.9F, 1.6F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && !isHostile() && source.getEntity() instanceof LivingEntity) burst();
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) {
            // the bite tears armour and leaves the wound weak
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack armour = le.getItemBySlot(slot);
                if (!armour.isEmpty() && armour.isDamageableItem()) armour.hurtAndBreak(6, le, slot);
            }
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), this);
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            openO = open;
            open += ((isHostile() ? 1.0F : 0.0F) - open) * 0.18F;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isHostile() ? SoundEvents.RAVAGER_AMBIENT : null;
    }

    @Override
    public float getVoicePitch() {
        return isHostile() ? 1.7F : 1.0F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOOL_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOOL_BREAK;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.WOOL_STEP, 0.4F, 1.2F);
    }
}
