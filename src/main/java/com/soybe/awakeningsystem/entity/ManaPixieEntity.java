package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Hada de Maná Silvana (Mana Pixie).
 * Diminuto ser feérico volador del Gran Árbol del Mundo (Yggdrasil / Reino Isekai).
 * - Vuela grácilmente batiendo sus 4 alas translúcidas mientras sostiene un orbe de maná concentrado.
 * - Regenera Maná y Salud a cazadores cercanos de forma pasiva cada pocos segundos.
 * - Puede capturarse en un Frasco de Vidrio para convertirse en Hada en Frasco.
 */
public class ManaPixieEntity extends PathfinderMob implements FlyingAnimal {

    private int blessCooldown = 40;

    public ManaPixieEntity(EntityType<? extends ManaPixieEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 16, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.FLYING_SPEED, 0.35D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.45D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide()) {
            if (this.random.nextFloat() < 0.35F) {
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.4D,
                        this.getY() + 0.3D + (this.random.nextDouble() - 0.5D) * 0.3D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.4D,
                        0.0D, 0.02D, 0.0D);
            }
            if (this.random.nextFloat() < 0.20F) {
                this.level().addParticle(ParticleTypes.INSTANT_EFFECT,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.3D,
                        this.getY() + 0.4D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.3D,
                        0.0D, 0.01D, 0.0D);
            }
        } else {
            if (--this.blessCooldown <= 0) {
                this.blessCooldown = 50 + this.random.nextInt(20);
                this.applyManaBlessing();
            }
        }
    }

    private void applyManaBlessing() {
        List<Player> nearby = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(7.0D));
        boolean blessedAny = false;

        for (Player player : nearby) {
            if (player.isAlive()) {
                if (player instanceof ServerPlayer sp) {
                    SystemData data = SystemEvents.data(sp);
                    if (data != null && data.energy() < data.maxEnergy()) {
                        data.restoreEnergy(15);
                        AwakeningSystemMod.sync(sp);
                        blessedAny = true;
                    }
                }
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false));
                blessedAny = true;
            }
        }

        if (blessedAny && this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getX(), this.getY() + 0.4D, this.getZ(),
                    8, 0.3D, 0.3D, 0.3D, 0.05D);
            this.level().playSound(null, this.blockPosition(), ModSounds.MANA_PIXIE_BLESS.get(),
                    SoundSource.NEUTRAL, 0.6F, 1.2F);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.GLASS_BOTTLE)) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            ItemStack bottled = new ItemStack(SystemItems.BOTTLED_MANA_PIXIE.get());
            if (!player.addItem(bottled)) {
                player.drop(bottled, false);
            }

            this.level().playSound(null, this.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH,
                    SoundSource.PLAYERS, 1.0F, 1.2F);
            this.level().playSound(null, this.blockPosition(), ModSounds.MANA_PIXIE_BLESS.get(),
                    SoundSource.PLAYERS, 0.9F, 1.4F);

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.3D, this.getZ(),
                        15, 0.25D, 0.25D, 0.25D, 0.08D);
            }

            this.discard();
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 0.3D, this.getZ(),
                    20, 0.5D, 0.5D, 0.5D, 0.1D);
        }
        return result;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MANA_PIXIE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.MANA_PIXIE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MANA_PIXIE_DEATH.get();
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }
}
