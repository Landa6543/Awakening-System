package com.soybe.awakeningsystem.entity.animal;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ManaChickenEntity extends Chicken {

    public ManaChickenEntity(EntityType<? extends Chicken> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Chicken.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D);
    }

    @Override
    public void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        ItemStack drop = new ItemStack(this.isOnFire() ?
                SystemItems.COOKED_MANA_CHICKEN_MEAT.get() : SystemItems.MANA_CHICKEN_MEAT.get(), 1);
        ItemEntity entity = new ItemEntity(level, this.getX(), this.getY() + 0.5D, this.getZ(), drop);
        level.addFreshEntity(entity);

        int feathers = 1 + this.random.nextInt(2);
        level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY() + 0.5D, this.getZ(),
                new ItemStack(Items.FEATHER, feathers)));
    }

    @Nullable
    @Override
    public Chicken getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.MANA_CHICKEN.get().create(level);
    }
}
