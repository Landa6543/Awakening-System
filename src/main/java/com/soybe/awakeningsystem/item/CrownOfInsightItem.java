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

/**
 * Corona de la Sabiduría (Crown of Insight) de Genshin Impact:
 * El material supremo para coronar talentos y desatar la maestría absoluta.
 * Clic derecho para consumir: Otorga Coronación Celestial, aumentando permanentemente el límite de habilidades
 * y otorgando una bendición celestial de combate por 20 minutos.
 */
public class CrownOfInsightItem extends Item {

    public CrownOfInsightItem(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            stack.shrink(1);
            SystemData data = SystemEvents.data(sp);

            for (String skillName : data.skills().keySet()) {
                int currentLvl = data.skills().get(skillName);
                if (currentLvl < 10) {
                    data.skills().put(skillName, currentLvl + 1);
                }
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 24000, 2, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 24000, 1, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.LUCK, 24000, 2, false, true, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 60, 0.6D, 0.8D, 0.6D, 0.25D);
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.5D, sp.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 2.0F, 0.8F);
            }

            sp.displayClientMessage(Component.translatable("item.awakening_system.crown_of_insight.crowned"), false);
            AwakeningSystemMod.sync(sp);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.badge").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.title").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.desc2"));
    }
}
