package com.soybe.awakeningsystem.world.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A townsperson of the medieval town: a player-shaped person wearing one of the town's skins. They stroll
 * around their home, open doors, look at whoever passes and greet a player who talks to them (right-click).
 * Their trade ({@link #job()}) is kept for the daily routines and shops they will get later.
 */
public class TownNpcEntity extends PathfinderMob {
    /** The skins, by index: textures/entity/town_npc/<name>.png */
    public static final String[] SKINS = {"smith", "maiden"};
    private static final int GREETINGS = 6;

    private static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(TownNpcEntity.class, EntityDataSerializers.INT);
    private String job = "";
    private BlockPos home = BlockPos.ZERO;

    public TownNpcEntity(EntityType<? extends TownNpcEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        if (this.getNavigation() instanceof GroundPathNavigation nav) nav.setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation nav = new GroundPathNavigation(this, level);
        nav.setCanOpenDoors(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(2, new MoveTowardsRestrictionGoal(this, 0.6));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.45));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(SKIN, 0);
    }

    public int skin() {
        return Math.floorMod(this.entityData.get(SKIN), SKINS.length);
    }

    public String job() {
        return job;
    }

    /** Settles this person at {@code home} with a trade and a skin; they stay within a few houses of it. */
    public void settle(BlockPos home, String job, int skin) {
        this.home = home.immutable();
        this.job = job == null ? "" : job;
        this.entityData.set(SKIN, skin);
        this.restrictTo(this.home, 14);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level().isClientSide) {
            int line = Math.floorMod(this.getId() * 31 + (int) (level().getGameTime() / 200), GREETINGS);
            player.sendSystemMessage(Component.translatable("entity.awakening_system.town_npc.greeting." + line, this.getDisplayName()));
            this.getLookControl().setLookAt(player);
            this.playSound(SoundEvents.VILLAGER_AMBIENT, 0.6F, 1.1F);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Skin", this.entityData.get(SKIN));
        tag.putString("Job", job);
        tag.putLong("Home", home.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SKIN, tag.getInt("Skin"));
        this.job = tag.getString("Job");
        this.home = BlockPos.of(tag.getLong("Home"));
        if (!home.equals(BlockPos.ZERO)) this.restrictTo(home, 14);
    }
}
