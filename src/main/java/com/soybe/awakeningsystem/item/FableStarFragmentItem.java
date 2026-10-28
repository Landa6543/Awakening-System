package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class FableStarFragmentItem extends Item {

    public FableStarFragmentItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.addExperience(3500);
            AwakeningSystemMod.sync(sp);

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.FIREWORK, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 30, 0.4D, 0.5D, 0.4D, 0.08D);
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.3D, 0.6D, 0.3D, 0.05D);

            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 1.5F);
            sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.2F);

            sp.displayClientMessage(Component.translatable("item.awakening_system.fable_star_fragment.use_message"), true);
            AwakeningSystemMod.notify(sp, "item.awakening_system.fable_star_fragment",
                    "item.awakening_system.fable_star_fragment.notify_desc", 0xFFFFD700);

            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.fable_star_fragment.title"));
        tooltip.add(Component.translatable("item.awakening_system.fable_star_fragment.desc"));
        tooltip.add(Component.translatable("item.awakening_system.fable_star_fragment.effect"));
    }
}
