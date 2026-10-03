package com.soybe.awakeningsystem.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.entity.PartEntity;

public class DragonBodyPart extends PartEntity<net.minecraft.world.entity.Entity> {
    public final String partName;
    private final EntityDimensions size;
    private final DragonPartParent partParent;

    public <T extends net.minecraft.world.entity.Entity & DragonPartParent> DragonBodyPart(T parent, String partName, float width, float height) {
        super(parent);
        this.partParent = parent;
        this.partName = partName;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {}

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(net.minecraft.world.entity.Entity attacker) {
        if (this.getParent() != null && this.getParent().hasPassenger(attacker)) {
            return true;
        }
        return super.skipAttackInteraction(attacker);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.getParent() != null && source.getEntity() != null && this.getParent().hasPassenger(source.getEntity())) {
            return false;
        }
        return this.partParent.hurtFromPart(this, source, amount);
    }

    @Override
    public boolean is(net.minecraft.world.entity.Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (this.getParent() instanceof AncientDragonEntity dragon) {
            return this.size.scale(dragon.getDragonScale() / 4.0F);
        }
        return this.size;
    }

    @Override
    protected boolean canAddPassenger(net.minecraft.world.entity.Entity passenger) {
        return false;
    }

    @Override
    public boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    public net.minecraft.world.InteractionResult interact(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (this.getParent() != null && this.getParent().hasPassenger(player)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (this.getParent() instanceof net.minecraft.world.entity.LivingEntity living) {
            return living.interact(player, hand);
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public net.minecraft.world.InteractionResult interactAt(net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.Vec3 vec, net.minecraft.world.InteractionHand hand) {
        if (this.getParent() != null && this.getParent().hasPassenger(player)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (this.getParent() instanceof net.minecraft.world.entity.LivingEntity living) {
            return living.interactAt(player, vec, hand);
        }
        return net.minecraft.world.InteractionResult.PASS;
    }
}
