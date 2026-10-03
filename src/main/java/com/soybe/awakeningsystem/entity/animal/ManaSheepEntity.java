package com.soybe.awakeningsystem.entity.animal;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ManaSheepEntity extends Sheep {

    public ManaSheepEntity(EntityType<? extends Sheep> entityType, Level level) {
        super(entityType, level);
        this.setColor(DyeColor.CYAN);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Sheep.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D);
    }

    @Override
    public void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        int count = 1 + this.random.nextInt(2);
        ItemStack drop = new ItemStack(this.isOnFire() ?
                SystemItems.COOKED_MANA_MUTTON.get() : SystemItems.MANA_MUTTON.get(), count);
        ItemEntity entity = new ItemEntity(level, this.getX(), this.getY() + 0.5D, this.getZ(), drop);
        level.addFreshEntity(entity);
    }

    @Nullable
    @Override
    public Sheep getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.MANA_SHEEP.get().create(level);
    }
}
