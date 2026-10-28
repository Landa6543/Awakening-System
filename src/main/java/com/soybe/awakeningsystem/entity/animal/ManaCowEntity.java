package com.soybe.awakeningsystem.entity.animal;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ManaCowEntity extends Cow {

    public ManaCowEntity(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.BUCKET) && !this.isBaby()) {
            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            ItemStack milkStack = ItemUtils.createFilledResult(held, player,
                    new ItemStack(SystemItems.MANA_MILK_BUCKET.get()));
            player.setItemInHand(hand, milkStack);

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 0.8D, this.getZ(),
                        15, 0.4D, 0.4D, 0.4D, 0.05D);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        int count = 1 + this.random.nextInt(3);
        ItemStack drop = new ItemStack(this.isOnFire() ?
                SystemItems.COOKED_MANA_BEEF.get() : SystemItems.MANA_BEEF.get(), count);
        ItemEntity entity = new ItemEntity(level, this.getX(), this.getY() + 0.5D, this.getZ(), drop);
        level.addFreshEntity(entity);

        if (this.random.nextFloat() < 0.65F) {
            level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY() + 0.5D, this.getZ(),
                    new ItemStack(Items.LEATHER, 1 + this.random.nextInt(2))));
        }
    }

    @Nullable
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.MANA_COW.get().create(level);
    }
}
