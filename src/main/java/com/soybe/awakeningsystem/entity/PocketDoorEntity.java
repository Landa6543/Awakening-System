package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.sanctuary.PocketSanctuaryManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Entidad física tridimensional de la Puerta a Dimensión de Bolsillo (Sección 29).
 * Representa una puerta normal física y sólida (marco de madera y hoja batiente con picaporte)
 * que conecta con el santuario privado sin efectos etéreos ni velos de energía.
 */
public class PocketDoorEntity extends Entity {

    private static final EntityDataAccessor<Boolean> IS_OPEN =
            SynchedEntityData.defineId(PocketDoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CLOSING =
            SynchedEntityData.defineId(PocketDoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SANCTUARY_SIDE =
            SynchedEntityData.defineId(PocketDoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(PocketDoorEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private float doorAngle = 0.0F;
    private float prevDoorAngle = 0.0F;
    public int cooldownTicks = 0;
    private int closingTicks = -1;

    public void setCooldownTicks(int ticks) {
        this.cooldownTicks = ticks;
    }

    public int getCooldownTicks() {
        return this.cooldownTicks;
    }

    public PocketDoorEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IS_OPEN, false);
        builder.define(IS_CLOSING, false);
        builder.define(IS_SANCTUARY_SIDE, false);
        builder.define(OWNER_UUID, Optional.empty());
    }

    public boolean isOpen() {
        return this.entityData.get(IS_OPEN);
    }

    public boolean isClosing() {
        return this.entityData.get(IS_CLOSING);
    }

    public void setClosing(boolean closing) {
        this.entityData.set(IS_CLOSING, closing);
    }

    public void setOpen(boolean open) {
        this.entityData.set(IS_OPEN, open);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
    }

    public boolean isSanctuarySide() {
        return this.entityData.get(IS_SANCTUARY_SIDE);
    }

    public void setSanctuarySide(boolean inside) {
        this.entityData.set(IS_SANCTUARY_SIDE, inside);
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public void setOwner(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public float getDoorAngle(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevDoorAngle, this.doorAngle);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        this.prevDoorAngle = this.doorAngle;
        float targetAngle = (isOpen() && !isClosing()) ? 90.0F : 0.0F;
        this.doorAngle = Mth.lerp(0.22F, this.doorAngle, targetAngle);

        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
        }

        if (!this.level().isClientSide) {
            // Manejo de la fase cinemática de cierre suave (~18 ticks)
            if (this.closingTicks > 0) {
                this.closingTicks--;
                if (this.closingTicks == 0) {
                    // La hoja de la puerta ha terminado de girar y está completamente cerrada contra el marco
                    setOpen(false);
                    setClosing(false);
                    this.closingTicks = -1;
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.9F);

                    if (isSanctuarySide()) {
                        if (getOwnerUUID() != null && this.getServer() != null) {
                            ServerPlayer sp = this.getServer().getPlayerList().getPlayer(getOwnerUUID());
                            if (sp != null) {
                                PocketSanctuaryManager.onSanctuaryDoorClosed(sp);
                                sp.displayClientMessage(Component.translatable("awakening_system.msg.santuario_puerta_cerrada_el_acceso_en"), true);
                            }
                        }
                    } else {
                        // En el Overworld, retirar la puerta una vez concluida su animación
                        if (getOwnerUUID() != null && this.getServer() != null) {
                            ServerPlayer sp = this.getServer().getPlayerList().getPlayer(getOwnerUUID());
                            if (sp != null) {
                                SystemEvents.data(sp).setSanctuaryExteriorDoorActive(false);
                                sp.displayClientMessage(Component.translatable("awakening_system.msg.puerta_cerrada_desvanecida"), true);
                            }
                        }
                        AABB cleanBox = new AABB(this.getX() - 8.0D, this.getY() - 4.0D, this.getZ() - 8.0D, this.getX() + 8.0D, this.getY() + 4.0D, this.getZ() + 8.0D);
                        List<PocketDoorEntity> duplicates = this.level().getEntitiesOfClass(PocketDoorEntity.class, cleanBox,
                            d -> !d.isSanctuarySide() && java.util.Objects.equals(d.getOwnerUUID(), this.getOwnerUUID()));
                        for (PocketDoorEntity dup : duplicates) {
                            dup.discard();
                        }
                        this.discard();
                        return;
                    }
                }
            }

            // 1. Purga automática de entidades duplicadas superpuestas
            if (this.tickCount % 5 == 0 && this.isAlive()) {
                AABB cleanBox = new AABB(this.getX() - 4.0D, this.getY() - 3.0D, this.getZ() - 4.0D, this.getX() + 4.0D, this.getY() + 3.0D, this.getZ() + 4.0D);
                List<PocketDoorEntity> nearby = this.level().getEntitiesOfClass(PocketDoorEntity.class, cleanBox,
                    d -> d != this && d.isAlive() && d.isSanctuarySide() == this.isSanctuarySide() && java.util.Objects.equals(d.getOwnerUUID(), this.getOwnerUUID()));
                for (PocketDoorEntity dup : nearby) {
                    dup.discard();
                }
            }

            // 2. Si es puerta exterior y el dueño la cerró desde el santuario, iniciar cierre suave y desvanecer
            if (!this.isSanctuarySide() && getOwnerUUID() != null) {
                if (this.getServer() != null) {
                    ServerPlayer sp = this.getServer().getPlayerList().getPlayer(getOwnerUUID());
                    if (sp != null) {
                        SystemData data = SystemEvents.data(sp);
                        if (!data.isSanctuaryExteriorDoorActive() && this.closingTicks < 0 && isOpen()) {
                            setOpen(false);
                            setClosing(true);
                            this.closingTicks = 18;
                        }
                    }
                }
            }

            // 3. Detección física al cruzar a través del vano abierto de la puerta
            if (isOpen() && !isClosing() && this.cooldownTicks == 0) {
                AABB triggerBox = new AABB(this.getX() - 0.7D, this.getY(), this.getZ() - 0.7D, this.getX() + 0.7D, this.getY() + 2.3D, this.getZ() + 0.7D);
                List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, triggerBox);
                long currentTime = this.level().getGameTime();
                for (ServerPlayer player : players) {
                    if (player.getPersistentData().getLong("pocket_door_cooldown") > currentTime) {
                        continue;
                    }
                    if (getOwnerUUID() == null || player.getUUID().equals(getOwnerUUID()) || player.hasPermissions(2)) {
                        this.cooldownTicks = 60;
                        player.getPersistentData().putLong("pocket_door_cooldown", currentTime + 60);
                        if (isSanctuarySide()) {
                            PocketSanctuaryManager.exitSanctuary(player, this);
                        } else {
                            PocketSanctuaryManager.enterSanctuary(player, this);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        UUID owner = getOwnerUUID();
        if (owner != null && !player.getUUID().equals(owner) && !player.hasPermissions(2)) {
            player.displayClientMessage(Component.translatable("awakening_system.msg.acceso_denegado_esta_puerta_pertenece_al"), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.6F);
            return InteractionResult.CONSUME;
        }

        if (isSanctuarySide()) {
            // ==========================================
            // DENTRO DEL SANTUARIO
            // ==========================================
            if (isOpen() && !isClosing()) {
                // Paso 2 del Roadmap: El cazador cierra la puerta desde adentro
                // Iniciar animación cinemática suave de cierre (se cerrará y retirará al completar los ticks)
                setClosing(true);
                this.closingTicks = 18;
                if (player instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("awakening_system.msg.santuario_cerrando_puerta_interior"), true);
                }
            } else if (!isOpen() && !isClosing()) {
                // Paso 3 del Roadmap: El cazador abre la puerta desde adentro para salir
                // -> Al abrirse, la puerta reaparece abierta en el Overworld en las coordenadas de origen
                setOpen(true);
                setClosing(false);
                this.closingTicks = -1;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (player instanceof ServerPlayer sp) {
                    PocketSanctuaryManager.onSanctuaryDoorOpened(sp);
                    sp.displayClientMessage(Component.translatable("awakening_system.msg.santuario_puerta_abierta_el_portal_de"), true);
                }
            }
        } else {
            // ==========================================
            // EN EL MUNDO EXTERIOR (OVERWORLD)
            // ==========================================
            // Shift + Clic derecho para retirar la puerta directamente
            if (player.isShiftKeyDown()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.9F);
                player.displayClientMessage(Component.translatable("awakening_system.msg.puerta_fisica_de_santuario_retirada"), true);
                this.discard();
                return InteractionResult.CONSUME;
            }

            if (isOpen() && !isClosing()) {
                // Paso 4 del Roadmap: El cazador cruza al Overworld y cierra la puerta desde afuera
                // Iniciar animación de cierre suave (se cerrará y desvanecerá al completar la animación)
                setClosing(true);
                this.closingTicks = 18;
                if (player instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("awakening_system.msg.cerrando_puerta_dimensional"), true);
                }
            } else if (!isOpen() && !isClosing()) {
                // Paso 1 del Roadmap: Abre la puerta desde afuera para cruzar al santuario
                setOpen(true);
                setClosing(false);
                this.closingTicks = -1;
                if (player instanceof ServerPlayer sp) {
                    SystemEvents.data(sp).setSanctuaryExteriorDoorActive(true);
                    sp.displayClientMessage(Component.translatable("awakening_system.msg.puerta_abierta_cruza_el_umbral_para"), true);
                }
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("IsOpen")) setOpen(tag.getBoolean("IsOpen"));
        if (tag.contains("IsSanctuarySide")) setSanctuarySide(tag.getBoolean("IsSanctuarySide"));
        if (tag.hasUUID("OwnerUUID")) setOwner(tag.getUUID("OwnerUUID"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("IsOpen", isOpen());
        tag.putBoolean("IsSanctuarySide", isSanctuarySide());
        UUID owner = getOwnerUUID();
        if (owner != null) tag.putUUID("OwnerUUID", owner);
    }
}
