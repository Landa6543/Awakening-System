package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ManaMilkBucketItem extends Item {

    public ManaMilkBucketItem(Properties properties) {
        super(properties.stacksTo(1).craftRemainder(Items.BUCKET));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.removeAllEffects();
            if (entity instanceof ServerPlayer player) {
                SystemData data = SystemEvents.data(player);
                data.restoreEnergy(35);
                AwakeningSystemMod.sync(player);
            }
        }
        return entity instanceof Player player && player.getAbilities().instabuild ? stack : new ItemStack(Items.BUCKET);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
