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
        this.setCustomName(Component.literal("§d§l✦ Paimon (La Mejor Compañera) ✦"));
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

        // Seguir al jugador vinculado suavemente al hombro derecho
        if (boundPlayerUuid != null && this.level() instanceof ServerLevel sl) {
            Player player = sl.getPlayerByUUID(boundPlayerUuid);
            if (player != null && player.isAlive() && player.level() == this.level()) {
                double distance = this.distanceTo(player);

                // Teletransportarse si está demasiado lejos
                if (distance > 32.0D) {
                    this.teleportTo(player.getX() + 1.0D, player.getY() + 1.2D, player.getZ() + 1.0D);
                    sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 0.5D, this.getZ(), 1, 0, 0, 0, 0);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    // Posición objetivo: hombro derecho flotante
                    Vec3 look = player.getLookAngle();
                    Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
                    double bob = Math.sin(this.tickCount * 0.15D) * 0.2D;
                    Vec3 targetPos = player.position().add(right.scale(1.2D)).add(0, 1.3D + bob, 0).subtract(look.scale(0.5D));

                    Vec3 diff = targetPos.subtract(this.position());
                    this.setDeltaMovement(diff.scale(0.25D));
                    this.setYRot(player.getYRot());
                    this.setXRot(0);
                }

                // Estela de estrellas de Paimon
                if (this.tickCount % 6 == 0) {
                    sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.3D, this.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.02D);
                }

                // Voz y diálogos ambientales aleatorios ocasionales
                if (this.tickCount % 600 == 0 && this.random.nextFloat() < 0.35F) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TALK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                // Diálogos y advertencias ambientales
                if (speechCooldown <= 0) {
                    if (player.getHealth() <= player.getMaxHealth() * 0.25F) {
                        player.displayClientMessage(Component.literal("§d§l[Paimon] §c¡Cuidado Viajero! ¡Tu salud está muy baja! ¡Paimon no quiere quedarse sin compañero!"), true);
                        sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TALK.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
                        speechCooldown = 200;
                    }
                }
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        // Si no está vinculada a nadie, vincular al jugador que interactúa
        if (boundPlayerUuid == null) {
            this.boundPlayerUuid = player.getUUID();
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.8D, this.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_SPAWN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            player.displayClientMessage(Component.literal("§d§l[Paimon] §f¡Hola! ¡Paimon será tu guía y mejor compañera en este mundo!"), false);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // Caso 1: Broma de "Comida de Emergencia" (si se agacha con espada o tijeras o tazón)
        if (player.isShiftKeyDown() && (itemInHand.is(Items.SHEARS) || itemInHand.is(Items.BOWL) || itemInHand.getItem() instanceof net.minecraft.world.item.SwordItem)) {
            if (this.level() instanceof ServerLevel sl) {
                if (player instanceof ServerPlayer sp) {
                    sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
                }
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 0.8D, this.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.02D);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), com.soybe.awakeningsystem.ModSounds.PAIMON_TALK.get(), SoundSource.VOICE, 1.2F, 1.0F);
                // Dropea una galleta de emergencia
                ItemStack cookie = new ItemStack(Items.COOKIE);
                cookie.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("§6Galleta de Emergencia de Paimon"));
                this.spawnAtLocation(cookie);
            }
            player.displayClientMessage(Component.literal("§d§l[Paimon] §c¡Oye! ¡Paimon NO es comida de emergencia! ¡Paimon es una compañera respetable!"), false);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // Caso 2: Alimentar a Paimon con cualquier comida
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
                player.displayClientMessage(Component.literal("§d§l[Paimon] §a¡Mmm, qué delicia! ¡La comida que me diste estaba increíble! ¡Paimon te da su bendición estelar (Suerte II y Maná)!"), false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // Caso 3: Clic derecho regular -> Abrir Menú Gacha / Deseos
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
