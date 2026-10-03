package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ManaFoodItem extends Item {

    private final int energyRestored;
    private final MobEffectInstance effect;

    public ManaFoodItem(Properties properties, int energyRestored, @Nullable MobEffectInstance effect) {
        super(properties);
        this.energyRestored = energyRestored;
        this.effect = effect;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (energyRestored > 0) {
                data.restoreEnergy(energyRestored);
            }
            if (effect != null) {
                player.addEffect(new MobEffectInstance(effect));
            }
            AwakeningSystemMod.sync(player);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
