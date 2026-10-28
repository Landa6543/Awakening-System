package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PaimonEntity extends PathfinderMob {
    private UUID boundPlayerUuid = null;
    private int speechCooldown = 0;

    public PaimonEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
        this.setCustomName(Component.translatable("entity.awakening_system.paimon.title"));
        this.setCustomNameVisible(true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.FLYING_SPEED, 0.60D)
                .add(Attributes.MOVEMENT_SPEED, 0.40D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    public void setBoundPlayer(Player player) {
        this.boundPlayerUuid = player.getUUID();
    }

    public UUID getBoundPlayerUuid() {
        return boundPlayerUuid;
    }

    @Override
    public void tick() {
        super.tick();

        if (speechCooldown > 0) speechCooldown--;

        if (!this.level().isClientSide() && this.isAlive() && this.tickCount % 20 == 0 && this.level() instanceof ServerLevel sl) {
            if (this.boundPlayerUuid != null) {
                var duplicates = sl.getEntitiesOfClass(PaimonEntity.class,
                        this.getBoundingBox().inflate(64.0D),
                        other -> other != this && other.isAlive() && (this.boundPlayerUuid.equals(other.getBoundPlayerUuid()) || other.getBoundPlayerUuid() == null));
                for (var dup : duplicates) {
                    if (this.getId() < dup.getId() || dup.getBoundPlayerUuid() == null) {
                        sl.sendParticles(ParticleTypes.POOF, dup.getX(), dup.getY() + 0.5D, dup.getZ(), 12, 0.2D, 0.2D, 0.2D, 0.05D);
                        dup.discard();
                    }
                }
            }
        }

        if (boundPlayerUuid != null && this.level() instanceof ServerLevel sl) {
            Player player = sl.getPlayerByUUID(boundPlayerUuid);
            if (player != null && player.isAlive() && player.level() == this.level()) {
                double distance = this.distanceTo(player);

                if (distance > 28.0D) {
                    Vec3 look = player.getLookAngle();
                    Vec3 targetSafe = player.position().subtract(look.scale(2.5D)).add(0, 1.3D, 0);
                    this.teleportTo(targetSafe.x, targetSafe.y, targetSafe.z);
                    sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 0.5D, this.getZ(), 1, 0, 0, 0, 0);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    double bob = Math.sin(this.tickCount * 0.12D) * 0.08D;
                    boolean playerMoving = player.getDeltaMovement().horizontalDistanceSqr() > 0.003D;

                    if (distance < 1.4D) {
                        Vec3 away = this.position().subtract(player.position()).normalize();
                        this.setDeltaMovement(away.x * 0.12D, 0.02D + bob, away.z * 0.12D);
                    } else if (playerMoving || distance > 4.2D) {
                        Vec3 look = player.getLookAngle();
                        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
                        double backDist = player.isUsingItem() ? 3.6D : 2.8D;
                        double sideDist = 1.8D;
                        double targetHeight = 1.3D + bob;

                        net.minecraft.core.BlockPos ceilingPos = net.minecraft.core.BlockPos.containing(player.getX(), player.getEyeY() + 1.2D, player.getZ());
                        if (this.level().getBlockState(ceilingPos).isSolid()) {
                            targetHeight = 0.4D + bob * 0.5D;
                        }

                        Vec3 targetPos = player.position()
                                .add(right.scale(sideDist))
                                .add(0, targetHeight, 0)
                                .subtract(look.scale(backDist));

                        if (this.level().getBlockState(net.minecraft.core.BlockPos.containing(targetPos)).isSolid()) {
                            targetPos = player.position().add(0, targetHeight, 0).subtract(look.scale(1.8D));
                        }

                        Vec3 diff = targetPos.subtract(this.position());
                        this.setDeltaMovement(diff.scale(0.18D));
                    } else {
                        double curY = this.getY();
                        double targetY = player.getY() + 1.2D + bob;
                        double yDiff = (targetY - curY) * 0.1D;
                        this.setDeltaMovement(this.getDeltaMovement().x * 0.75D, yDiff, this.getDeltaMovement().z * 0.75D);
                    }

                    double dx = player.getX() - this.getX();
                    double dz = player.getZ() - this.getZ();
                    float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                    float smoothYaw = Mth.rotLerp(0.25F, this.getYRot(), targetYaw);
                    this.setYRot(smoothYaw);
                    this.yHeadRot = smoothYaw;
                    this.yBodyRot = smoothYaw;

                    double dy = (player.getEyeY() - 0.2D) - (this.getY() + 0.5D);
                    double horizDist = Math.sqrt(dx * dx + dz * dz);
                    float targetPitch = (float) (-(Mth.atan2(dy, horizDist) * (180.0D / Math.PI)));
                    this.setXRot(Mth.rotLerp(0.25F, this.getXRot(), targetPitch));
                }

                if (this.tickCount % 6 == 0) {
                    sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.3D, this.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.02D);
                }

                if (speechCooldown <= 0) {
                    if (player.getHealth() <= player.getMaxHealth() * 0.25F) {
                        player.displayClientMessage(Component.translatable("entity.awakening_system.paimon.low_health"), true);
                        sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TALK.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
                        speechCooldown = 240;
                    } else if (this.level().isRaining() && this.random.nextInt(600) == 0) {
                        player.displayClientMessage(Component.translatable("entity.awakening_system.paimon.rain"), true);
                        sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TALK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        speechCooldown = 600;
                    } else if (this.tickCount % 700 == 0 && this.random.nextFloat() < 0.30F) {
                        sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TALK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        speechCooldown = 300;
                    }
                }
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (boundPlayerUuid == null) {
            this.boundPlayerUuid = player.getUUID();
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.8D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_SPAWN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            player.displayClientMessage(Component.translatable("entity.awakening_system.paimon.greeting"), false);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (player.isShiftKeyDown() && (itemInHand.is(Items.SHEARS) || itemInHand.is(Items.BOWL) || itemInHand.getItem() instanceof net.minecraft.world.item.SwordItem)) {
            if (this.level() instanceof ServerLevel sl) {
                if (player instanceof ServerPlayer sp) {
                    sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
                }
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 0.8D, this.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.02D);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TALK.get(), SoundSource.VOICE, 1.2F, 1.0F);
                ItemStack cookie = new ItemStack(Items.COOKIE);
                cookie.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("item.awakening_system.paimon_emergency_cookie"));
                this.spawnAtLocation(cookie);
            }
            player.displayClientMessage(Component.translatable("entity.awakening_system.paimon.emergency_food"), false);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (!itemInHand.isEmpty() && (itemInHand.has(net.minecraft.core.component.DataComponents.FOOD) || itemInHand.is(Items.COOKIE) || itemInHand.is(Items.CAKE) || itemInHand.is(Items.BREAD) || itemInHand.is(Items.COOKED_BEEF))) {
            if (!this.level().isClientSide()) {
                itemInHand.shrink(1);
                if (player instanceof ServerPlayer sp) {
                    SystemData data = SystemEvents.data(sp);
                    data.restoreEnergy(40);
                    sp.addEffect(new MobEffectInstance(MobEffects.LUCK, 3600, 1, false, false, true));
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0, false, false, true));
                    AwakeningSystemMod.sync(sp);
                }
                if (this.level() instanceof ServerLevel sl) {
                    if (player instanceof ServerPlayer sp2) {
                        sp2.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
                    }
                    sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.8D, this.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.5D, this.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_THANK.get(), SoundSource.VOICE, 1.2F, 1.0F);
                }
                player.displayClientMessage(Component.translatable("entity.awakening_system.paimon.fed_blessing"), false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (!this.level().isClientSide() && player instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TALK.get(), SoundSource.VOICE, 1.0F, 1.0F);
            }
            com.soybe.awakeningsystem.GachaManager.openGachaScreen(sp);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (boundPlayerUuid != null) {
            tag.putUUID("bound_player_uuid", boundPlayerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("bound_player_uuid")) {
            boundPlayerUuid = tag.getUUID("bound_player_uuid");
        }
    }
}
