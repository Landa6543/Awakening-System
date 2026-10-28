package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.limbo.LimboWorldManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Puerta del Limbo (The Limbo Door / Returning Rift):
 * Estructura y umbral de escape legendario del Limbo.
 * Diseñado y modelado originalmente en Blockbench por el usuario.
 * Interactuar o cruzar la puerta devuelve al cazador a su punto de reaparición del Overworld.
 */
public class LimboDoorEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> UNLOCKED =
            net.minecraft.network.syncher.SynchedEntityData.defineId(LimboDoorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    public LimboDoorEntity(EntityType<? extends LimboDoorEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(UNLOCKED, false);
    }

    private int lockedAlertCooldown = 0;
    private int guardiansDefeated = 0;
    private int respawnCooldownTicks = -1; // Cooldown de 1 minuto tras derrotar a los 2 ángeles

    public boolean isUnlocked() {
        return this.entityData.get(UNLOCKED) || this.guardiansDefeated >= 2;
    }

    public void setUnlocked(boolean unlocked) {
        this.entityData.set(UNLOCKED, unlocked);
    }

    public int getGuardiansDefeated() {
        return this.guardiansDefeated;
    }

    public int getRespawnCooldownTicks() {
        return this.respawnCooldownTicks;
    }

    public void setRespawnCooldownTicks(int ticks) {
        this.respawnCooldownTicks = ticks;
    }

    public void notifyGuardianKilled(ServerPlayer killer) {
        if (this.isUnlocked()) return;

        this.guardiansDefeated++;
        if (this.guardiansDefeated >= 2) {
            this.setUnlocked(true);
            this.respawnCooldownTicks = 1200; // 60 segundos (1 minuto) de tregua/enfriamiento antes de permitir nuevo respawn
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.2F, 1.0F);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.2F, 1.2F);

            for (ServerPlayer player : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D))) {
                player.displayClientMessage(
                        Component.literal("§b§l[PUERTA DEL LIMBO] §a§l¡Los 2 Ángeles Guardianes han sido derrotados! El umbral de escape está abierto."),
                        false
                );
                player.displayClientMessage(
                        Component.literal("§a§l¡Portal Desbloqueado! Cruza la puerta para volver al Overworld."),
                        true
                );
            }
        } else {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
            for (ServerPlayer player : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D))) {
                player.displayClientMessage(
                        Component.literal("§e§l[PUERTA DEL LIMBO] §6Ángel derrotado (" + this.guardiansDefeated + "/2). Derrota al siguiente ángel para abrir el portal."),
                        true
                );
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (lockedAlertCooldown > 0) {
            lockedAlertCooldown--;
        }
        if (this.isUnlocked() && this.respawnCooldownTicks > 0) {
            this.respawnCooldownTicks--;
            if (this.respawnCooldownTicks == 0) {
                // Pasó el minuto de gracia: la puerta se sella nuevamente para futuros cazadores
                this.setUnlocked(false);
                this.guardiansDefeated = 0;
                this.respawnCooldownTicks = -1;
            }
        }

        // Perímetro Sagrado de la Puerta del Limbo (Radio de 50 bloques):
        // Las criaturas del Limbo (excepto los Ángeles Guardianes) son repelidas por una barrera de vacío y almas.
        if (!this.level().isClientSide() && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            double sanctuaryRadius = 50.0D;
            net.minecraft.world.phys.AABB sanctuaryBox = this.getBoundingBox().inflate(sanctuaryRadius);
            for (net.minecraft.world.entity.monster.Monster mob : sl.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, sanctuaryBox)) {
                if (mob instanceof LimboOphanimEntity) {
                    continue; // Los Ángeles Ofanim son los guardianes sagrados legítimos de la puerta
                }
                double distSq = mob.distanceToSqr(this.getX(), mob.getY(), this.getZ());
                if (distSq < sanctuaryRadius * sanctuaryRadius) {
                    mob.setTarget(null);
                    double dx = mob.getX() - this.getX();
                    double dz = mob.getZ() - this.getZ();
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist < 0.001D) {
                        dx = 1.0D;
                        dz = 0.0D;
                        dist = 1.0D;
                    }
                    double pushStrength = Math.max(0.4D, (sanctuaryRadius - dist) * 0.06D);
                    mob.setDeltaMovement((dx / dist) * pushStrength, 0.15D, (dz / dist) * pushStrength);
                    mob.hurtMarked = true;

                    if (this.tickCount % 8 == 0) {
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, mob.getX(), mob.getY() + 0.8D, mob.getZ(), 4, 0.2D, 0.3D, 0.2D, 0.02D);
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, mob.getX(), mob.getY() + 0.5D, mob.getZ(), 3, 0.1D, 0.1D, 0.1D, 0.01D);
                    }
                }
            }
        }

        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    protected net.minecraft.world.phys.AABB makeBoundingBox() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        // Hitbox monumental ampliada para cubrir todo el marco, las piedras laterales,
        // la hoja de la puerta y el umbral de paso (centrado, multiplicador 3.55x del modelo).
        double minLocalX = -3.6D;
        double maxLocalX = 3.6D;
        double minLocalZ = -2.5D;
        double maxLocalZ = 2.5D;
        double height = 10.5D;

        float yawRad = (float) Math.toRadians(this.getYRot());
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);

        double[] cX = { minLocalX, minLocalX, maxLocalX, maxLocalX };
        double[] cZ = { minLocalZ, maxLocalZ, minLocalZ, maxLocalZ };

        double minWorldX = Double.POSITIVE_INFINITY;
        double maxWorldX = Double.NEGATIVE_INFINITY;
        double minWorldZ = Double.POSITIVE_INFINITY;
        double maxWorldZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < 4; i++) {
            double wx = x + (cX[i] * cos - cZ[i] * sin);
            double wz = z + (cX[i] * sin + cZ[i] * cos);
            if (wx < minWorldX) minWorldX = wx;
            if (wx > maxWorldX) maxWorldX = wx;
            if (wz < minWorldZ) minWorldZ = wz;
            if (wz > maxWorldZ) maxWorldZ = wz;
        }

        return new net.minecraft.world.phys.AABB(minWorldX, y, minWorldZ, maxWorldX, y + height, maxWorldZ);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isShiftKeyDown()) {
                serverPlayer.setRespawnPosition(this.level().dimension(), this.blockPosition(), serverPlayer.getYRot(), true, true);
                serverPlayer.displayClientMessage(Component.literal("§b§l[Puerta del Limbo] §aPunto de reaparición establecido."), true);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
            tryPassDoor(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (this.getBoundingBox().intersects(player.getBoundingBox())) {
                tryPassDoor(serverPlayer);
            }
        }
    }

    /**
     * Intenta cruzar la puerta. Requiere derrotar a 2 Ángeles Ofanim guardianes.
     */
    private void tryPassDoor(ServerPlayer player) {
        if (!isUnlocked()) {
            if (lockedAlertCooldown <= 0) {
                int needed = Math.max(1, 2 - this.guardiansDefeated);
                player.displayClientMessage(
                        Component.literal("§c§l[Puerta del Limbo Sellada] §eDerrota a los 2 Ángeles Guardianes para desbloquear el portal. (" + this.guardiansDefeated + "/2 derrotados)"),
                        true
                );
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.0F, 0.65F);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9F, 0.8F);
                lockedAlertCooldown = 30; // 1.5 segundos de enfriamiento entre avisos
            }
            return;
        }

        // Guardianes derrotados: Retorno concedido
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.1F);

        LimboWorldManager.returnFromLimbo(player);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.guardiansDefeated = compound.getInt("GuardiansDefeated");
        this.setUnlocked(compound.getBoolean("Unlocked"));
        if (compound.contains("RespawnCooldownTicks")) {
            this.respawnCooldownTicks = compound.getInt("RespawnCooldownTicks");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("GuardiansDefeated", this.guardiansDefeated);
        compound.putBoolean("Unlocked", this.isUnlocked());
        compound.putInt("RespawnCooldownTicks", this.respawnCooldownTicks);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
