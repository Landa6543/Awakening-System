package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class FoundationBuildingPillItem extends Item {
    public FoundationBuildingPillItem(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.addCultivationQi(2500);

            int subLevel = data.cultivationSubLevel();
            int realm = data.dantianRealm();
            if (data.cultivationQi() >= data.maxCultivationQi() && subLevel < 9) {
                data.setCultivationSubLevel(subLevel + 1);
                data.setCultivationQi(0);
                sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.4F);
                sp.displayClientMessage(Component.translatable("awakening_system.cultivation.sublevel_success", data.cultivationSubLevel(), SystemEvents.getDantianRealmComponent(realm)), true);
            } else {
                sp.displayClientMessage(Component.translatable("item.awakening_system.foundation_building_pill.refined"), true);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 0));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 1));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.5D, 0.6D, 0.5D, 0.05D);

            sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.5F);

            AwakeningSystemMod.notify(sp, "awakening_system.item.foundation_building_pill.title", "awakening_system.item.foundation_building_pill.desc", 0xFF2E8B57);
            AwakeningSystemMod.sync(sp);

            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.awakening_system.foundation_building_pill.title").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        tooltipComponents.add(Component.translatable("item.awakening_system.foundation_building_pill.quote"));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("item.awakening_system.foundation_building_pill.effects_header").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("item.awakening_system.foundation_building_pill.effect1"));
        tooltipComponents.add(Component.translatable("item.awakening_system.foundation_building_pill.effect2"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
