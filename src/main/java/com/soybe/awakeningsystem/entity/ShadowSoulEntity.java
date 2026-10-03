package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShadowSoulEntity extends Entity {
    private static final EntityDataAccessor<String> MOB_NAME =
            SynchedEntityData.defineId(ShadowSoulEntity.class, EntityDataSerializers.STRING);

    private String mobEntityTypeId = "minecraft:zombie";
    private UUID ownerUUID = null;
    private int lifeTicks = 1200; // 60 segundos de vida antes de disiparse
    private int extractionAttempts = 0;
    private int shadowRank = 0;
    private final Map<EquipmentSlot, ItemStack> savedEquipment = new HashMap<>();

    public int getExtractionAttempts() {
        return extractionAttempts;
    }

    public void incrementExtractionAttempts() {
        this.extractionAttempts++;
    }

    public int getShadowRank() {
        return shadowRank;
    }

    public void setShadowRank(int rank) {
        this.shadowRank = rank;
        setMobName(getMobName());
    }

    public ShadowSoulEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public ShadowSoulEntity(Level level, double x, double y, double z, Mob defeated, UUID ownerUUID) {
        this(ModEntities.SHADOW_SOUL.get(), level);
        this.setPos(x, y, z);
        this.ownerUUID = ownerUUID;
        this.mobEntityTypeId = EntityType.getKey(defeated.getType()).toString();
        this.shadowRank = com.soybe.awakeningsystem.ShadowRank.determineInitialRank(defeated).getLevel();

        String name = defeated.getCustomName() != null ? defeated.getCustomName().getString() : defeated.getType().getDescription().getString();
        this.setMobName(name);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = defeated.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                savedEquipment.put(slot, stack.copy());
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MOB_NAME, "Enemigo Caído");
    }

    public void setMobName(String name) {
        this.entityData.set(MOB_NAME, name);
        com.soybe.awakeningsystem.ShadowRank r = com.soybe.awakeningsystem.ShadowRank.byLevel(this.shadowRank);
        this.setCustomName(Component.literal("§8§l✦ ALMA DE " + r.getPrefix() + " §9" + name + " §8[Presiona 'V' (Surge)] ✦"));
        this.setCustomNameVisible(true);
    }

    public String getMobName() {
        return this.entityData.get(MOB_NAME);
    }

    public String getMobEntityTypeId() {
        return mobEntityTypeId;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public Map<EquipmentSlot, ItemStack> getSavedEquipment() {
        return savedEquipment;
    }

    @Override
    public void tick() {
        super.tick();

        // Efectos visuales de niebla y fuego de almas en el suelo
        if (this.level().isClientSide) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
            double py = this.getY() + this.random.nextDouble() * 0.6D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;

            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.03D, 0);
            if (this.random.nextBoolean()) {
                this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.02D, 0);
            }
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SCULK_SOUL, px, py, pz, 0, 0.04D, 0);
            }
        } else {
            lifeTicks--;
            if (lifeTicks <= 0) {
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.3D, this.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.02D);
                }
                this.discard();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("MobEntityTypeId")) {
            this.mobEntityTypeId = tag.getString("MobEntityTypeId");
        }
        if (tag.contains("MobName")) {
            this.setMobName(tag.getString("MobName"));
        }
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("LifeTicks")) {
            this.lifeTicks = tag.getInt("LifeTicks");
        }
        if (tag.contains("ExtractionAttempts")) {
            this.extractionAttempts = tag.getInt("ExtractionAttempts");
        }
        if (tag.contains("ShadowRank")) {
            this.shadowRank = tag.getInt("ShadowRank");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("MobEntityTypeId", this.mobEntityTypeId);
        tag.putString("MobName", this.getMobName());
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putInt("ExtractionAttempts", this.extractionAttempts);
        tag.putInt("ShadowRank", this.shadowRank);
    }
}
