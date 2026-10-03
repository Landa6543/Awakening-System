package com.soybe.awakeningsystem.entity.animal;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ManaPigEntity extends Pig {

    public ManaPigEntity(EntityType<? extends Pig> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pig.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    public void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        int count = 1 + this.random.nextInt(3);
        ItemStack drop = new ItemStack(this.isOnFire() ?
                SystemItems.COOKED_MANA_PORKCHOP.get() : SystemItems.MANA_PORKCHOP.get(), count);
        ItemEntity entity = new ItemEntity(level, this.getX(), this.getY() + 0.5D, this.getZ(), drop);
        level.addFreshEntity(entity);
    }

    @Nullable
    @Override
    public Pig getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.MANA_PIG.get().create(level);
    }
}
