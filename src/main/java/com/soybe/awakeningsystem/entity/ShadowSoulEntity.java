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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ShadowSoulEntity extends Entity {
    private static final EntityDataAccessor<String> MOB_NAME =
            SynchedEntityData.defineId(ShadowSoulEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<Component>> MOB_NAME_COMP =
            SynchedEntityData.defineId(ShadowSoulEntity.class, EntityDataSerializers.OPTIONAL_COMPONENT);

    private String mobEntityTypeId = "minecraft:zombie";
    private UUID ownerUUID = null;
    private int lifeTicks = 1200;
    private int extractionAttempts = 0;
    private int shadowRank = 0;
    private int rpgMobLevel = 1;
    private double mobMaxHealth = 20.0;
    private double mobAttackDamage = 3.0;
    private double mobArmor = 0.0;
    private double mobArmorToughness = 0.0;
    private double mobKnockbackResistance = 0.0;
    private double mobMovementSpeed = 0.25;
    private CompoundTag savedEntityNbt = new CompoundTag();
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
        setMobNameComponent(getMobNameComponent());
    }

    public int getRpgMobLevel() {
        return rpgMobLevel;
    }

    public double getMobMaxHealth() {
        return mobMaxHealth;
    }

    public double getMobAttackDamage() {
        return mobAttackDamage;
    }

    public double getMobArmor() {
        return mobArmor;
    }

    public double getMobArmorToughness() {
        return mobArmorToughness;
    }

    public double getMobKnockbackResistance() {
        return mobKnockbackResistance;
    }

    public double getMobMovementSpeed() {
        return mobMovementSpeed;
    }

    public CompoundTag getSavedEntityNbt() {
        return savedEntityNbt;
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

        if (defeated.getPersistentData().contains("rpg_mob_level")) {
            this.rpgMobLevel = Math.max(1, defeated.getPersistentData().getInt("rpg_mob_level"));
        } else {
            this.rpgMobLevel = 1;
        }

        this.mobMaxHealth = Math.max(20.0, (double) defeated.getMaxHealth());

        AttributeInstance atk = defeated.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null && atk.getValue() > 0.0) {
            this.mobAttackDamage = atk.getValue();
        } else if (this.rpgMobLevel > 1) {
            this.mobAttackDamage = 3.0 * (1.0 + this.rpgMobLevel * 0.05) + this.rpgMobLevel * 0.35;
        } else {
            this.mobAttackDamage = 3.0;
        }

        AttributeInstance arm = defeated.getAttribute(Attributes.ARMOR);
        this.mobArmor = arm != null ? arm.getValue() : 0.0;

        AttributeInstance tgh = defeated.getAttribute(Attributes.ARMOR_TOUGHNESS);
        this.mobArmorToughness = tgh != null ? tgh.getValue() : 0.0;

        AttributeInstance kb = defeated.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        this.mobKnockbackResistance = kb != null ? kb.getValue() : 0.0;

        AttributeInstance spd = defeated.getAttribute(Attributes.MOVEMENT_SPEED);
        this.mobMovementSpeed = spd != null ? spd.getValue() : 0.25;

        try {
            CompoundTag nbt = new CompoundTag();
            defeated.saveWithoutId(nbt);
            nbt.putBoolean("ModelDead", false);
            nbt.putInt("DeadProg", 0);
            nbt.putInt("DeathTime", 0);
            nbt.putBoolean("NoAI", false);
            nbt.remove("DeathTime");
            this.savedEntityNbt = nbt;
        } catch (Throwable ignored) {}

        Component originalComp = defeated.getCustomName() != null ? defeated.getCustomName().copy() : defeated.getType().getDescription().copy();
        this.setMobNameComponent(originalComp);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = defeated.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                savedEquipment.put(slot, stack.copy());
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MOB_NAME, "Fallen Enemy");
        builder.define(MOB_NAME_COMP, Optional.empty());
    }

    public void setMobNameComponent(Component component) {
        if (component == null) {
            component = Component.literal("Fallen Enemy");
        }
        this.entityData.set(MOB_NAME_COMP, Optional.of(component));
        this.entityData.set(MOB_NAME, component.getString());
        com.soybe.awakeningsystem.ShadowRank r = com.soybe.awakeningsystem.ShadowRank.byLevel(this.shadowRank);
        Component displayName;
        if (this.rpgMobLevel > 1) {
            displayName = Component.literal("§e[Lv. " + this.rpgMobLevel + "] ").append(component);
        } else {
            displayName = component;
        }
        this.setCustomName(Component.translatable("awakening_system.shadow.soul_name_prompt", r.getDisplayName(), displayName));
        this.setCustomNameVisible(true);
    }

    public Component getMobNameComponent() {
        Optional<Component> opt = this.entityData.get(MOB_NAME_COMP);
        if (opt.isPresent() && opt.get() != null) {
            return opt.get();
        }
        String s = getMobName();
        if (s != null && !s.isEmpty()) {
            return Component.literal(s);
        }
        return Component.translatable("awakening_system.shadow.unknown_soul");
    }

    public void setMobName(String name) {
        String cleanName = (name != null && !name.isEmpty()) ? name : "Fallen Enemy";
        this.entityData.set(MOB_NAME, cleanName);
        this.entityData.set(MOB_NAME_COMP, Optional.of(Component.literal(cleanName)));
        com.soybe.awakeningsystem.ShadowRank r = com.soybe.awakeningsystem.ShadowRank.byLevel(this.shadowRank);
        Component displayName;
        if (this.rpgMobLevel > 1) {
            displayName = Component.literal("§e[Lv. " + this.rpgMobLevel + "] " + cleanName);
        } else {
            displayName = Component.literal(cleanName);
        }
        this.setCustomName(Component.translatable("awakening_system.shadow.soul_name_prompt", r.getDisplayName(), displayName));
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
        if (tag.contains("ShadowRank")) {
            this.shadowRank = tag.getInt("ShadowRank");
        }
        if (tag.contains("RpgMobLevel")) {
            this.rpgMobLevel = tag.getInt("RpgMobLevel");
        }
        if (tag.contains("MobMaxHealth")) {
            this.mobMaxHealth = tag.getDouble("MobMaxHealth");
        }
        if (tag.contains("MobAttackDamage")) {
            this.mobAttackDamage = tag.getDouble("MobAttackDamage");
        }
        if (tag.contains("MobArmor")) {
            this.mobArmor = tag.getDouble("MobArmor");
        }
        if (tag.contains("MobArmorToughness")) {
            this.mobArmorToughness = tag.getDouble("MobArmorToughness");
        }
        if (tag.contains("MobKnockbackResistance")) {
            this.mobKnockbackResistance = tag.getDouble("MobKnockbackResistance");
        }
        if (tag.contains("MobMovementSpeed")) {
            this.mobMovementSpeed = tag.getDouble("MobMovementSpeed");
        }
        if (tag.contains("SavedEntityNbt")) {
            this.savedEntityNbt = tag.getCompound("SavedEntityNbt");
        }
        if (tag.contains("SavedEquipment")) {
            CompoundTag eqTag = tag.getCompound("SavedEquipment");
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (eqTag.contains(slot.getName())) {
                    ItemStack.parse(this.registryAccess(), eqTag.getCompound(slot.getName()))
                            .ifPresent(stack -> this.savedEquipment.put(slot, stack));
                }
            }
        }
        if (tag.contains("MobNameJson")) {
            try {
                Component c = Component.Serializer.fromJson(tag.getString("MobNameJson"), this.registryAccess());
                if (c != null) {
                    this.setMobNameComponent(c);
                }
            } catch (Throwable ignored) {}
        } else if (tag.contains("MobName")) {
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("MobEntityTypeId", this.mobEntityTypeId);
        tag.putString("MobName", this.getMobName());
        tag.putInt("RpgMobLevel", this.rpgMobLevel);
        tag.putDouble("MobMaxHealth", this.mobMaxHealth);
        tag.putDouble("MobAttackDamage", this.mobAttackDamage);
        tag.putDouble("MobArmor", this.mobArmor);
        tag.putDouble("MobArmorToughness", this.mobArmorToughness);
        tag.putDouble("MobKnockbackResistance", this.mobKnockbackResistance);
        tag.putDouble("MobMovementSpeed", this.mobMovementSpeed);
        if (this.savedEntityNbt != null && !this.savedEntityNbt.isEmpty()) {
            tag.put("SavedEntityNbt", this.savedEntityNbt);
        }
        if (!this.savedEquipment.isEmpty()) {
            CompoundTag eqTag = new CompoundTag();
            for (Map.Entry<EquipmentSlot, ItemStack> entry : this.savedEquipment.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    eqTag.put(entry.getKey().getName(), entry.getValue().saveOptional(this.registryAccess()));
                }
            }
            tag.put("SavedEquipment", eqTag);
        }
        try {
            tag.putString("MobNameJson", Component.Serializer.toJson(this.getMobNameComponent(), this.registryAccess()));
        } catch (Throwable ignored) {}
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putInt("ExtractionAttempts", this.extractionAttempts);
        tag.putInt("ShadowRank", this.shadowRank);
    }
}
