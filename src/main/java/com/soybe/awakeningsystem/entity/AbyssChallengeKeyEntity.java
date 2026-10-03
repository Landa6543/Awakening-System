package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.abyss.SpiralAbyssManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

/**
 * Entidad 3D Canónica de la Llave Roja de Desafío / Espada del Abismo de Genshin Impact.
 * Mecanismo levitante carmesí con dial rúnico orbital en el centro de la arena.
 * Al hacer clic derecho o golpearla, se activa el desafío con fanfarria y explosión escarlata.
 */
public class AbyssChallengeKeyEntity extends LivingEntity {

    public AbyssChallengeKeyEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("§c§l✦ INICIAR DESAFÍO ✦"));
        this.setCustomNameVisible(true);
        this.addTag("abyss_challenge_key");
        this.addTag("no_shadow_extraction");
        this.addTag("no_block_destroy");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {}

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            triggerActivation(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && source.getEntity() instanceof ServerPlayer sp) {
            triggerActivation(sp);
            return true;
        }
        return false;
    }

    private void triggerActivation(ServerPlayer player) {
        if (this.isRemoved()) return;

        boolean triggered = SpiralAbyssManager.triggerChallengeKey(player, this);
        if (triggered) {
            if (this.level() instanceof ServerLevel sl) {
                // Onda expansiva de partículas carmesí y sonido épico
                sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.2, this.getZ(), 2, 0.4, 0.4, 0.4, 0.0);
                sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.2, this.getZ(), 50, 0.8, 0.8, 0.8, 0.1);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.2, this.getZ(), 30, 0.6, 0.6, 0.6, 0.08);
                sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.2, this.getZ(), 1, 0, 0, 0, 0);

                sl.playSound(null, this.getX(), this.getY() + 1.2, this.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
                sl.playSound(null, this.getX(), this.getY() + 1.2, this.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
            }
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) {
            // Partículas carmesí de prueba ascendentes
            double angle = (this.tickCount * 0.08D);
            double r = 0.9D;
            double px = this.getX() + Math.cos(angle) * r;
            double pz = this.getZ() + Math.sin(angle) * r;
            double py = this.getY() + 0.4D + (this.tickCount % 40) * 0.04D;

            this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0.0D, 0.02D, 0.0D);
            if (this.random.nextFloat() < 0.25F) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        this.getY() + 0.8D + this.random.nextDouble() * 0.8D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        0.0D, 0.02D, 0.0D);
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
