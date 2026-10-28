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

public class HeavenlyEnlightenmentPillItem extends Item {
    public HeavenlyEnlightenmentPillItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.addCultivationQi(4500);

            for (int i = 0; i < 8; i++) {
                if (!data.isMeridianOpen(i)) {
                    data.openMeridian(i);
                    sp.displayClientMessage(Component.translatable("item.awakening_system.heavenly_enlightenment_pill.meridian_opened", (i + 1)), false);
                    break;
                }
            }

            int subLevel = data.cultivationSubLevel();
            int realm = data.dantianRealm();
            if (data.cultivationQi() >= data.maxCultivationQi() && subLevel < 9) {
                data.setCultivationSubLevel(subLevel + 1);
                data.setCultivationQi(0);
                sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.4F);
                sp.displayClientMessage(Component.translatable("awakening_system.cultivation.sublevel_success", data.cultivationSubLevel(), SystemEvents.getDantianRealmComponent(realm)), true);
            } else {
                sp.displayClientMessage(Component.translatable("item.awakening_system.heavenly_enlightenment_pill.refined"), true);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 6000, 0));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.08D);
            sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 35, 0.5D, 0.5D, 0.5D, 0.1D);

            sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.6F, 1.3F);
            sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);

            AwakeningSystemMod.notify(sp, "awakening_system.item.heavenly_enlightenment_pill.title", "awakening_system.item.heavenly_enlightenment_pill.desc", 0xFF9370DB);
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
        tooltipComponents.add(Component.translatable("item.awakening_system.heavenly_enlightenment_pill.title").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        tooltipComponents.add(Component.translatable("item.awakening_system.heavenly_enlightenment_pill.quote"));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("item.awakening_system.heavenly_enlightenment_pill.effects_header").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("item.awakening_system.heavenly_enlightenment_pill.effect1"));
        tooltipComponents.add(Component.translatable("item.awakening_system.heavenly_enlightenment_pill.effect2"));
        tooltipComponents.add(Component.translatable("item.awakening_system.heavenly_enlightenment_pill.effect3"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
