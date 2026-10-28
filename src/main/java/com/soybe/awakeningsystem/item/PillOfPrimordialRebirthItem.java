package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.OpenClassSelectionPayload;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PillOfPrimordialRebirthItem extends Item {
    public PillOfPrimordialRebirthItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);

            data.resetHunterClass();
            AwakeningSystemMod.sync(sp);

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.0D);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 80, 1.0D, 1.2D, 1.0D, 0.15D);
            sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 100, 1.5D, 1.5D, 1.5D, 0.2D);

            sp.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
            sp.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.2F);

            sp.sendSystemMessage(Component.translatable("item.awakening_system.pill_of_primordial_rebirth.divider"));
            sp.sendSystemMessage(Component.translatable("item.awakening_system.pill_of_primordial_rebirth.title"));
            sp.sendSystemMessage(Component.translatable("item.awakening_system.pill_of_primordial_rebirth.body"));
            sp.sendSystemMessage(Component.translatable("item.awakening_system.pill_of_primordial_rebirth.prompt"));
            sp.sendSystemMessage(Component.translatable("item.awakening_system.pill_of_primordial_rebirth.divider"));

            PacketDistributor.sendToPlayer(sp, new OpenClassSelectionPayload());

            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.awakening_system.primordial_rebirth_pill.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltipComponents.add(Component.translatable("item.awakening_system.primordial_rebirth_pill.quote"));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("item.awakening_system.primordial_rebirth_pill.effects_header").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("item.awakening_system.primordial_rebirth_pill.desc1"));
        tooltipComponents.add(Component.translatable("item.awakening_system.primordial_rebirth_pill.desc2"));
        tooltipComponents.add(Component.translatable("item.awakening_system.primordial_rebirth_pill.desc3"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
