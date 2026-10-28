package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

public class BloodQiTotemEntity extends LivingEntity {
    private int pulseCooldown = 20;

    public BloodQiTotemEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable("entity.awakening_system.blood_qi_totem.name"));
        this.setCustomNameVisible(true);
        this.addTag("blood_sect_totem");
        this.addTag("no_shadow_extraction");
        this.addTag("no_block_destroy");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
            double py = this.getY() + 0.4D + this.random.nextDouble() * 1.6D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;
            this.level().addParticle(ParticleTypes.CRIMSON_SPORE, px, py, pz, 0.0D, 0.05D, 0.0D);
            if (this.random.nextFloat() < 0.25F) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0.0D, 0.03D, 0.0D);
            }
        } else {
            this.pulseCooldown--;
            if (this.pulseCooldown <= 0) {
                this.pulseCooldown = 20;
                pulseQiAura();
            }
        }
    }

    private void pulseQiAura() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        AABB buffBox = this.getBoundingBox().inflate(24.0D, 8.0D, 24.0D);
        List<Mob> allies = sl.getEntitiesOfClass(Mob.class, buffBox,
                m -> m.isAlive() && (m.getTags().contains("blood_sect") || m instanceof BloodPatriarchBossEntity || m instanceof BloodSectDiscipleEntity));

        for (Mob ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 45, 1, false, false));
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 45, 0, false, false));
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 45, 0, false, false));
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, ally.getX(), ally.getY() + 1.0D, ally.getZ(), 4, 0.2D, 0.3D, 0.2D, 0.02D);
        }

        AABB debuffBox = this.getBoundingBox().inflate(16.0D, 6.0D, 16.0D);
        List<ServerPlayer> players = sl.getEntitiesOfClass(ServerPlayer.class, debuffBox, ServerPlayer::isAlive);

        for (ServerPlayer player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 45, 0, false, false));
        }

        if (this.random.nextFloat() < 0.35F) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 0.6F, 0.5F);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.FALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(), 2, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 1.0D, this.getZ(), 50, 0.8D, 1.2D, 0.8D, 0.1D);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 1.4F, 0.9F);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0F, 1.2F);

            AABB fortressArea = this.getBoundingBox().inflate(50.0D, 20.0D, 50.0D);
            List<BloodQiTotemEntity> remainingTotems = sl.getEntitiesOfClass(BloodQiTotemEntity.class, fortressArea,
                    t -> t != this && t.isAlive());

            int remaining = remainingTotems.size();
            List<ServerPlayer> nearbyPlayers = sl.getEntitiesOfClass(ServerPlayer.class, fortressArea, ServerPlayer::isAlive);

            if (remaining == 0) {
                for (ServerPlayer sp : nearbyPlayers) {
                    sp.displayClientMessage(Component.translatable("awakening_system.entity.blood_qi_totem.all_banners_fallen"), true);
                    sp.playNotifySound(SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.4F, 0.8F);
                }
            } else {
                for (ServerPlayer sp : nearbyPlayers) {
                    sp.displayClientMessage(Component.translatable("awakening_system.entity.blood_qi_totem.banner_destroyed", remaining), true);
                    sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8F, 1.4F);
                }
            }
        }
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
